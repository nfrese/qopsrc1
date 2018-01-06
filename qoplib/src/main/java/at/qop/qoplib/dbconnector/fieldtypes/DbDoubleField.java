package at.qop.qoplib.dbconnector.fieldtypes;

import at.qop.qoplib.dbconnector.DbRecord;

public class DbDoubleField extends DbFieldAbstract {

	public String[] expectedTypeName() {
		return new String[] { "float8", "double" };
	}
	
	public Double get(DbRecord rec)
	{
		return (Double)rec.values[ix];
	}
	
}
