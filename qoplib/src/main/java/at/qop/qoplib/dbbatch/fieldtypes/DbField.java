package at.qop.qoplib.dbbatch.fieldtypes;

import at.qop.qoplib.dbbatch.DbTable;

public abstract class DbField {
	
	public DbTable table;
	public int ix;
	public String name;
	
	public void checkFieldType(String typeName) {
		String exp = expectedTypeName();
		if (!exp.equalsIgnoreCase(typeName)) throw new IllegalArgumentException("field " + name + " expected type:" + exp + " but got " + typeName);
	}

	protected abstract String expectedTypeName();
	
}
