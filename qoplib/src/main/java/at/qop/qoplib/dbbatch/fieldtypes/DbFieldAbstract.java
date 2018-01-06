package at.qop.qoplib.dbbatch.fieldtypes;

import java.util.Arrays;

import at.qop.qoplib.dbbatch.DbTable;

/**
 * see https://www.postgresql.org/message-id/AANLkTikkkxN%2B-UUiGVTzj8jdfS4PdpB8_tDONMFHNqHk%40mail.gmail.com
 */
public abstract class DbFieldAbstract {
	
	public DbTable table;
	public int ix;
	public String name;
	
	public void checkFieldType(String typeName) {
		boolean found = false;
		String[] exps = expectedTypeName();
		for (String exp : exps) {
			if (exp.equalsIgnoreCase(typeName)) found=true;
		}
		if (!found) throw new IllegalArgumentException("field " + name + " expected types:" + Arrays.toString(exps) + " but got " + typeName);
	}

	protected abstract String[] expectedTypeName();
	
}
