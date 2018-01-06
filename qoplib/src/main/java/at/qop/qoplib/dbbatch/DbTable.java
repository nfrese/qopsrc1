package at.qop.qoplib.dbbatch;

import at.qop.qoplib.dbbatch.fieldtypes.DbField;

public class DbTable {

	private static final int[] EMPTY_INT_ARR = new int[0];
	private static final String[] EMPTY_STRINGARR = new String[0];
	
	public String[] colNames = EMPTY_STRINGARR;
	public int[] sqlTypes = EMPTY_INT_ARR;
	public String[] typeNames = EMPTY_STRINGARR;
	
	public void init(int cols)
	{
		this.sqlTypes = new int[cols];
		this.colNames = new String[cols];	
		this.typeNames = new String[cols];
	}

	public <T extends DbField> T findField(String colName, Class<T> clazz) {
		T col;
		try {
			col = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		col.table = this;
		col.name = colName;
		for (int i=0;i<colNames.length;i++)
		{
			if (colName.equals(colNames[i])) {
				col.ix = i;
				col.checkFieldType(typeNames[i]);
				return col; 
			}
		}
		return null;
	}
	
}
