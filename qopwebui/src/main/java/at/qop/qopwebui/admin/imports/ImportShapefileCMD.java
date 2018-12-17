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

import java.nio.file.Path;

import at.qop.qoplib.ConfigFile;
import at.qop.qoplib.Utils;

public class ImportShapefileCMD {
	
	public final Path path;
	public boolean importFlag=true;
	public String tableName;
	public int srid = 4326;
	public String encoding = "LATIN1";
	public String warning = null;
	
	public ImportShapefileCMD(Path path) {
		super();
		this.path = path;
		tableName = proposeTableName();
	}
	
	public String getFilename()
	{
		return path.getFileName().toString();
	}

	private String proposeTableName() {
		return getFilename().toLowerCase().replaceAll("\\.shp$", "");
	}

	public String cmd(ConfigFile cfgFile) {
		String cmd = "shp2pgsql -d -w -I -s %SRID% -W \"%ENCODING%\" %SHAPEFILE% %TABLENAME%";
		cmd += " | psql -h %HOST% -U %USER_NAME% -d %DB% -p %PORT%";
		
		cmd = cmd.replace("%SRID%", this.srid+"");
		cmd = cmd.replace("%ENCODING%", Utils.uxCmdStringEscape(this.encoding));
		cmd = cmd.replace("%SHAPEFILE%", Utils.uxCmdStringEscape(this.path +""));
		cmd = cmd.replace("%TABLENAME%", Utils.uxCmdStringEscape("public." + this.tableName));
		
		cmd = cmd.replace("%HOST%", Utils.uxCmdStringEscape(cfgFile.getDbHost()));
		cmd = cmd.replace("%USER_NAME%", Utils.uxCmdStringEscape(cfgFile.getDbUserName()));
		cmd = cmd.replace("%DB%", Utils.uxCmdStringEscape(cfgFile.getDb()));
		cmd = cmd.replace("%PORT%", cfgFile.getPort()+"");
		return cmd;
	}
}