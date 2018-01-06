package at.qop.qoplib.dbbatch.fieldtypes;

import at.qop.qoplib.dbbatch.DbRecord;

public class DbDoubleField extends DbFieldAbstract {

	public String[] expectedTypeName() {
		return new String[] { "float8", "double" };
	}
	
	public Double get(DbRecord rec)
	{
		return (Double)rec.values[ix];
	}
	
}
