package at.qop.qopwebui.admin.forms.exports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button;

import at.qop.qoplib.ConfigFile;
import at.qop.qoplib.TmpWorkingDir;
import at.qop.qopwebui.components.ConfirmationDialog;
import at.qop.qopwebui.components.DownloadDialog;
import at.qop.qopwebui.components.ExecDialog;
import at.qop.qopwebui.components.InfoDialog;

public class ExportShapefiles {

	private final List<String> tableNames;
	private TmpWorkingDir tmpDir;
	private File zipFile;
	
	public ExportShapefiles(List<String> tableNames) {
		super();
		this.tableNames = tableNames;
	}

	public void run()
	{
		tmpDir = new TmpWorkingDir();
		tmpDir.create();

		ConfigFile cfgFile = ConfigFile.read();

		List<String> cmds = new ArrayList<>();

		for (String tableName : tableNames)
		{
			ExportShapefile s = new ExportShapefile(tmpDir.getPath() , tableName);
			String cmd = s.exportCmd(cfgFile);
			cmds.add(cmd);
		}

		ExecDialog execImp = new ExecDialog("Shape-Dateien aus der Datenbank exportieren");
		execImp.show();

		execImp.executeCommands(cmds.iterator(), new String[] {"PGPASSWORD=" + cfgFile.getDbPasswd()}, tmpDir.dir);
		execImp.onOK = (exit1) -> {
			
			zipFile = new File(tmpDir.dir, "downloadshapes.zip");
			
			ExecDialog execZip = new ExecDialog("Einpacken");
			execZip.show();
			execZip.executeCommand("zip -r " + zipFile.getName() + " *", null, tmpDir.dir);
			execZip.onOK = (exit) -> {

				StreamResource streamResource = createResource();
				ConfirmationDialog dod = new DownloadDialog("Download", streamResource.getFilename(), streamResource)
						.cancel(() -> { cleanup(); })
						.ok((e) -> { cleanup(); });
				dod.show();
			};
			
			execZip.onExit = () -> {
				cleanup();
			};
		};
		execImp.onExit = () -> { cleanup(); };
	}


	private void cleanup() {
		try {
			tmpDir.cleanUp();
			tmpDir = null;
			zipFile = null;
			new InfoDialog("Info", "Exportverzeichnis wurde gelöscht!").show();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}


	protected void init(String filename) {
		Button downloadButton = new Button("Download image");

		StreamResource myResource = null; // createResource();
		FileDownloader fileDownloader = new FileDownloader(myResource);
		fileDownloader.extend(downloadButton);

		//setContent(downloadButton);
	}

	private StreamResource createResource() {
		String downloadFilename = "qop_export_" + tableNames.stream().collect(Collectors.joining("_")) + ".zip";
		if (downloadFilename.length() > 200)
		{
			downloadFilename = "qop_multiexport.zip";
		}
		
		return new StreamResource(new StreamSource() {
			private static final long serialVersionUID = 1L;

			@Override
			public InputStream getStream() {
				try {
					return new FileInputStream(zipFile);
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
		}, downloadFilename);
	}

}
