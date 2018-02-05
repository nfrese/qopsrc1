package at.qop.qopwebui.admin.forms.exports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;

import at.qop.qoplib.ConfigFile;
import at.qop.qoplib.TmpWorkingDir;
import at.qop.qopwebui.components.ConfirmationDialog;
import at.qop.qopwebui.components.DownloadDialog;
import at.qop.qopwebui.components.ExecDialog;
import at.qop.qopwebui.components.InfoDialog;

public class DumpDatabase {

	private final List<String> tableNames;
	private TmpWorkingDir tmpDir;
	private File zipFile;
	
	public DumpDatabase(List<String> tableNames) {
		super();
		this.tableNames = tableNames;
	}

	public DumpDatabase() {
		super();
		this.tableNames = Collections.emptyList(); // complete db
	}
	
	public void run()
	{
		tmpDir = new TmpWorkingDir();
		tmpDir.create();

		ConfigFile cfgFile = ConfigFile.read();


		DumpDatabaseCMD s = new DumpDatabaseCMD(tmpDir.getPath(), tableNames);
		String cmd = s.cmd(cfgFile);

		ExecDialog execImp = new ExecDialog("Datenbank Backup erstellen");
		execImp.show();

		execImp.executeCommand(cmd, new String[] {"PGPASSWORD=" + cfgFile.getDbPasswd()}, tmpDir.dir);
		execImp.onOK = (exit1) -> {
			
			zipFile = new File(tmpDir.dir, "downloaddump.zip");
			
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

	private StreamResource createResource() {
		String downloadFilename = "qop_dump.zip";
		if (this.tableNames.size() == 0)
		{
			downloadFilename = "qop_dump_all.zip";
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
