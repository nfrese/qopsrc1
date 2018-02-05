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