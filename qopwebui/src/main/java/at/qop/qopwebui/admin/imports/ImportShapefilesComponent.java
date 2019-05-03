/* 
 * Copyright (C) 2018 Norbert Frese
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
*/

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.ProgressListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;

import at.qop.qoplib.Config;
import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.TmpWorkingDir;
import at.qop.qoplib.dbconnector.metadata.QopDBMetadata;
import at.qop.qoplib.dbconnector.metadata.QopDBTable;
import at.qop.qoplib.domains.IGenericDomain;
import at.qop.qopwebui.components.ExecDialog;
import at.qop.qopwebui.components.ExecDialogNext;
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
		ExecDialog execUnzip = new ExecDialogNext("Entpacken");
		execUnzip.show();
		execUnzip.executeCommand("unzip " + zipFile.getName() , null, tmpDir.dir);
		execUnzip.onOK = (exit) -> {
			Path directory = tmpDir.getPath();
			List<ImportShapefileCMD> shapeFiles = new ArrayList<>();				
			try {

				Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						if (file.getFileName().toString().toLowerCase().endsWith(".shp"))
						{
							shapeFiles.add(new ImportShapefileCMD(file));
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
				for (ImportShapefileCMD shape : shapeFiles)
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
				Config cfgFile = Config.read();

				List<String> cmds = new ArrayList<>();

				for (ImportShapefileCMD s : shapeFiles)
				{
					if (s.importFlag)
					{
						String cmd = s.cmd(cfgFile);
						cmds.add(cmd);
					}
				}

				ExecDialog execImp = new ExecDialog("Shape-Dateien in die Datenbank einspielen");
				execImp.show();
				Map<String, String> addEnv = new HashMap<>();
				addEnv.put("PGPASSWORD", cfgFile.getDbPasswd());
				execImp.executeCommands(cmds.iterator(), addEnv, tmpDir.dir);
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
