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

package at.qop.qopwebui.admin.forms.exports.raster;

import java.nio.file.Path;

import at.qop.qoplib.Config;
import at.qop.qoplib.Utils;
import at.qop.qopwebui.admin.forms.exports.ExportFileCMD;

public class ExportRasterCMD extends ExportFileCMD {
	
	public ExportRasterCMD(Path path, String tableName) {
		super(path, tableName);
	}

	@Override
	public String cmd(Config cfgFile) {
		String schema = cfgFile.getDbSchema();
		String cmd = "gdal_translate -of GTiff PG:\"host=%HOST% port=%PORT% dbname=%DB% user=%USER_NAME% password=%PASSWORD% schema="+schema+" table=%TABLE% column='rast' mode=2\" %TIFFILE%";
		cmd = cmd.replace("%TIFFILE%", Utils.uxCmdStringEscape(this.path + "/" + this.tableName + ".tif"));
		cmd = cmd.replace("%TABLE%", Utils.uxCmdStringEscape(this.tableName));
		
		cmd = cmd.replace("%HOST%", Utils.uxCmdStringEscape(cfgFile.getDbHost()));
		cmd = cmd.replace("%USER_NAME%", Utils.uxCmdStringEscape(cfgFile.getDbUserName()));
		cmd = cmd.replace("%PASSWORD%", Utils.uxCmdStringEscape(cfgFile.getDbPasswd()));
		cmd = cmd.replace("%DB%", Utils.uxCmdStringEscape(cfgFile.getDb()));
		cmd = cmd.replace("%PORT%", cfgFile.getPort()+"");
		return cmd;
	}
}