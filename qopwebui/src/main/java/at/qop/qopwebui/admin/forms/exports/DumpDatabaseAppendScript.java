package at.qop.qopwebui.admin.forms.exports;

import java.io.File;
import java.util.List;

import at.qop.qopwebui.components.ExecDialog;

public class DumpDatabaseAppendScript extends DumpDatabase {

	public final String appendSql;


	public DumpDatabaseAppendScript(List<String> tableNames, String appendSql) {
		super(tableNames);
		this.appendSql = appendSql;
	}

	@Override
	protected void afterDump(ExecDialog execImp, File dir) {
		execImp.executeCommand("echo \"" + appendSql + "\" >> dump.sql", null, dir); 
	}
	
}
