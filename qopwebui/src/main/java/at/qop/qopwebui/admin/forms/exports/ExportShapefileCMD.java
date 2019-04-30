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

import at.qop.qoplib.Config;
import at.qop.qoplib.Utils;

public class ExportShapefileCMD {
	
	public final Path path;
	public final String tableName;
	
	public ExportShapefileCMD(Path path, String tableName) {
		super();
		this.path = path;
		this.tableName = tableName;
	}
	
	public String getFilename()
	{
		return path.getFileName().toString();
	}

	public String cmd(Config cfgFile) {
	
		String cmd = "pgsql2shp -f %SHAPEFILE% -h %HOST% -u %USER_NAME% -p %PORT% %DB% %QRY%";
		
		cmd = cmd.replace("%SHAPEFILE%", Utils.uxCmdStringEscape(this.path + "/" + this.tableName));
		cmd = cmd.replace("%MAPPINGFILE%", Utils.uxCmdStringEscape(/* this.path + "/" + */ this.tableName + ".map"));
		cmd = cmd.replace("%QRY%", Utils.uxCmdStringEscape("select * from public." + this.tableName));
		
		cmd = cmd.replace("%HOST%", Utils.uxCmdStringEscape(cfgFile.getDbHost()));
		cmd = cmd.replace("%USER_NAME%", Utils.uxCmdStringEscape(cfgFile.getDbUserName()));
		cmd = cmd.replace("%DB%", Utils.uxCmdStringEscape(cfgFile.getDb()));
		cmd = cmd.replace("%PORT%", cfgFile.getPort()+"");
		return cmd;
	}
}