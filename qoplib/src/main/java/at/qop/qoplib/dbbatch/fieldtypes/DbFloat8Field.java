package at.qop.qoplib.dbbatch.fieldtypes;

import at.qop.qoplib.dbbatch.DbRecord;

public class DbFloat8Field extends DbFieldAbstract {

	public String[] expectedTypeName() {
		return new String[] {"float8"};
	}
	
	public Double get(DbRecord rec)
	{
		return (Double)rec.values[ix];
	}
	
}
