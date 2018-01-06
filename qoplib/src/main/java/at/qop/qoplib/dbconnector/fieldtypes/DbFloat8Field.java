package at.qop.qoplib.dbconnector.fieldtypes;

import at.qop.qoplib.dbconnector.DbRecord;

public class DbFloat8Field extends DbFieldAbstract {

	public String[] expectedTypeName() {
		return new String[] {"float8"};
	}
	
	public Double get(DbRecord rec)
	{
		return (Double)rec.values[ix];
	}
	
}
