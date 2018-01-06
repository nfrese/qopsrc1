package at.qop.qoplib.dbbatch;

import at.qop.qoplib.dbbatch.fieldtypes.DbFloat8Field;
import at.qop.qoplib.dbbatch.fieldtypes.DbDoubleField;
import at.qop.qoplib.dbbatch.fieldtypes.DbFieldAbstract;
import at.qop.qoplib.dbbatch.fieldtypes.DbFloat4Field;
import at.qop.qoplib.dbbatch.fieldtypes.DbGeometryField;
import at.qop.qoplib.dbbatch.fieldtypes.DbTextField;

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

	public DbGeometryField geometryField(String colName) {
		return this.field(colName, DbGeometryField.class);
	}

	public DbDoubleField doubleField(String colName) {
		return this.field(colName, DbDoubleField.class);
	}
	
	public DbFloat8Field float8Field(String colName) {
		return this.field(colName, DbFloat8Field.class);
	}

	public DbFloat4Field float4Field(String colName) {
		return this.field(colName, DbFloat4Field.class);
	}
	
	public DbTextField textField(String colName) {
		return this.field(colName, DbTextField.class);
	}
	
	public <T extends DbFieldAbstract> T field(String colName, Class<T> clazz) {
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
