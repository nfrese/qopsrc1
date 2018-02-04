package at.qop.qopwebui.admin.imports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.ProgressListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;

import at.qop.qoplib.ConfigFile;
import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.TmpWorkingDir;
import at.qop.qoplib.dbconnector.metadata.QopDBMetadata;
import at.qop.qoplib.dbconnector.metadata.QopDBTable;
import at.qop.qoplib.domains.IGenericDomain;
import at.qop.qopwebui.components.ExecDialog;
import at.qop.qopwebui.components.InfoDialog;

public class ImportShapefilesComponent extends Panel implements Receiver, SucceededListener, ProgressListener{
	
	private static final long serialVersionUID = 1L;
	
	private VerticalLayout vl;
	private TmpWorkingDir tmpDir;
	private File zipFile;
	private ProgressBar pgbar;
	
	public void init()
	{
		vl = new VerticalLayout();
		
		Upload upload = new Upload("Gezippte Shape-Dateien importieren", this);
		upload.addProgressListener(this);
		upload.addSucceededListener(this);
		vl.addComponent(upload);
		
		pgbar = new ProgressBar();
		vl.addComponent(pgbar);
		this.setContent(vl);
	}

	@Override
	public void uploadSucceeded(SucceededEvent event) {
		ExecDialog execUnzip = new ExecDialog("Entpacken");
		execUnzip.show();
		execUnzip.executeCommand("unzip " + zipFile.getName() , null, tmpDir.dir);
		execUnzip.onOK = (exit) -> {
			Path directory = tmpDir.getPath();
			List<ImportShapefile> shapeFiles = new ArrayList<>();				
			try {

				Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						if (file.getFileName().toString().toLowerCase().endsWith(".shp"))
						{
							shapeFiles.add(new ImportShapefile(file));
						}
						return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			IGenericDomain gd = LookupSessionBeans.genericDomain();

			QopDBMetadata meta = gd.getMetadata();
			for (QopDBTable table : meta.tables)
			{
				for (ImportShapefile shape : shapeFiles)
				{
					if (shape.tableName.equalsIgnoreCase(table.name))
					{
						shape.importFlag = false;
						shape.warning = table.name + " existiert bereits";
					}
				}
			}

			ImportShapefilesDialog isfd = new ImportShapefilesDialog("Auswahl der Shape-Dateien ", shapeFiles);
			isfd.onDone = () -> {
				ConfigFile cfgFile = ConfigFile.read();

				List<String> cmds = new ArrayList<>();

				for (ImportShapefile s : shapeFiles)
				{
					if (s.importFlag)
					{
						String cmd = s.importCmd(cfgFile);
						cmds.add(cmd);
					}
				}

				ExecDialog execImp = new ExecDialog("Shape-Dateien in die Datenbank einspielen");
				execImp.show();
				execImp.executeCommands(cmds.iterator(), new String[] {"PGPASSWORD=" + cfgFile.getDbPasswd()}, tmpDir.dir);
				execImp.onOK = (exit1) -> {
					cleanup();
				};
				execImp.onExit = () -> { cleanup(); };
			};
			isfd.onExit = () -> { cleanup(); };
			isfd.show();
		};
		
		execUnzip.onExit = () -> {
			cleanup();
		};
	}

	private void cleanup() {
		try {
			tmpDir.cleanUp();
			tmpDir = null;
			zipFile = null;
			new InfoDialog("Info", "Importverzeichnis wurde gelöscht!").show();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public OutputStream receiveUpload(String filename, String mimeType) {
		if (tmpDir != null) throw new RuntimeException("Import already running!");
		if (!mimeType.equals("application/zip")) throw new RuntimeException("mimeType != application/zip: " + mimeType);
		
		tmpDir = new TmpWorkingDir();
		tmpDir.create();
		try {
			zipFile = new File(tmpDir.dir, filename);
			System.out.println("downloading to zipfile " + zipFile);
			return new FileOutputStream(zipFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void updateProgress(long readBytes, long contentLength) {
		if (contentLength > 0)
			pgbar.setValue(readBytes/contentLength);
	}
	

}
