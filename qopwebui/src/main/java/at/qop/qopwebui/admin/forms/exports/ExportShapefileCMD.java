package at.qop.qopwebui.admin.forms.exports;

import java.nio.file.Path;

import at.qop.qoplib.ConfigFile;
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

	public String cmd(ConfigFile cfgFile) {
	
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