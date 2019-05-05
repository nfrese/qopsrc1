package at.qop.qopwebui.admin.forms.exports;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class DumpDatabaseAppendScript extends DumpDatabase {

	public final String appendSql;


	public DumpDatabaseAppendScript(List<String> tableNames, String appendSql) {
		super(tableNames);
		this.appendSql = appendSql;
	}

	@Override
	protected Iterator<String> addCmdAfterDump(String cmd) {
		return Arrays.asList(cmd, "echo \"" + appendSql + "\" >> dump.sql").iterator();
	}
	
}
