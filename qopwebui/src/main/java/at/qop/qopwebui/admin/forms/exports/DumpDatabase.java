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

package at.qop.qopwebui.admin.forms.exports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.InputStreamFactory;

import at.qop.qoplib.Config;
import at.qop.qoplib.TmpWorkingDir;
import at.qop.qopwebui.components.ConfirmationDialog;
import at.qop.qopwebui.components.DownloadDialog;
import at.qop.qopwebui.components.ExecDialog;
import at.qop.qopwebui.components.ExecDialogNext;
import at.qop.qopwebui.components.InfoDialog;

public class DumpDatabase {

	private final List<String> tableNames;
	private TmpWorkingDir tmpDir;
	private File zipFile;
	
	public DumpDatabase() {
		super();
		this.tableNames = Collections.emptyList(); // complete db
	}
	
	public DumpDatabase(List<String> tableNames) {
		super();
		this.tableNames = tableNames;
	}
	
	public void run()
	{
		tmpDir = new TmpWorkingDir();
		tmpDir.create();

		try {
			URL inputUrl = getClass().getResource("/at/qop/qoplib/docker/Dockerfile");
			File dest = new File(tmpDir.dir, "Dockerfile");
			FileUtils.copyURLToFile(inputUrl, dest);
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		
		Config cfgFile = Config.read();


		DumpDatabaseCMD s = new DumpDatabaseCMD(tmpDir.getPath(), tableNames);
		String cmd = s.cmd(cfgFile);

		ExecDialog execImp = new ExecDialogNext("Datenbank Backup erstellen");
		execImp.show();

		Map<String, String> addEnv = new HashMap<>();
		addEnv.put("PGPASSWORD", cfgFile.getDbPasswd());		
		
		execImp.executeCommands(addCmdAfterDump(cmd), addEnv, tmpDir.dir);
		
		execImp.onOK = (exit1) -> {
			
			zipFile = new File(tmpDir.dir, "downloaddump.zip");
			
			ExecDialog execZip = new ExecDialogNext("Einpacken");
			execZip.show();
			execZip.executeCommand("zip -r " + zipFile.getName() + " *", null, tmpDir.dir);
			execZip.onOK = (exit) -> {

				StreamResource streamResource = createResource();
				ConfirmationDialog dod = new DownloadDialog("Download", streamResource.getName(), streamResource)
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

	protected Iterator<String> addCmdAfterDump(String cmd) {
		return Arrays.asList(cmd).iterator();
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
		
		return new StreamResource(downloadFilename, new InputStreamFactory() {
			private static final long serialVersionUID = 1L;

			@Override
			public InputStream createInputStream() {
				try {
					return new FileInputStream(zipFile);
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

}
