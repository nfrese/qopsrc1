package at.qop.qopwebui.admin.forms.exports;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;

import at.qop.qoplib.ConfigFile;
import at.qop.qoplib.TmpWorkingDir;
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
			cleanup();
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

	//	    private StreamResource createResource() {
	//	        return new StreamResource(new StreamSource() {
	//	            @Override
	//	            public InputStream getStream() {
	//	                String text = "My image";
	//
	//	                BufferedImage bi = new BufferedImage(100, 30, BufferedImage.TYPE_3BYTE_BGR);
	//	                bi.getGraphics().drawChars(text.toCharArray(), 0, text.length(), 10, 20);
	//
	//	                try {
	//	                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	//	                    ImageIO.write(bi, "png", bos);
	//	                    return new ByteArrayInputStream(bos.toByteArray());
	//	                } catch (IOException e) {
	//	                    e.printStackTrace();
	//	                    return null;
	//	                }
	//
	//	            }
	//	        }, "myImage.png");
	//	    }
	//	}

}
