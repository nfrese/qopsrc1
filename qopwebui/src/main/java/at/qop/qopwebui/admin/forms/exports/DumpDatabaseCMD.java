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

import java.nio.file.Path;
import java.util.List;

import at.qop.qoplib.ConfigFile;
import at.qop.qoplib.Utils;

public class DumpDatabaseCMD {
	
	public final Path path;
	public final List<String> tableNames;
	
	public DumpDatabaseCMD(Path path, List<String> tableNames) {
		super();
		this.path = path;
		this.tableNames = tableNames;
	}
	
	public String getFilename()
	{
		return path.getFileName().toString();
	}

	public String cmd(ConfigFile cfgFile) {
	
		StringBuilder tablesString = new StringBuilder();
		
		tableNames.forEach(tname -> {
			tablesString.append(" -t ");
			tablesString.append(Utils.uxCmdStringEscape(tname));
		});
		
		String cmd = "pg_dump %TABLESSTRING% -h %HOST% -U %USER_NAME% -p %PORT% %DB% > dump.sql";
		
		cmd = cmd.replace("%TABLESSTRING%", tablesString);
		cmd = cmd.replace("%HOST%", Utils.uxCmdStringEscape(cfgFile.getDbHost()));
		cmd = cmd.replace("%USER_NAME%", Utils.uxCmdStringEscape(cfgFile.getDbUserName()));
		cmd = cmd.replace("%DB%", Utils.uxCmdStringEscape(cfgFile.getDb()));
		cmd = cmd.replace("%PORT%", cfgFile.getPort()+"");
		return cmd;
	}
}