package at.qop.qoplib.dbconnector.fieldtypes;

import at.qop.qoplib.dbconnector.DbRecord;

public class DbFloat4Field extends DbFieldAbstract {

	public String[] expectedTypeName() {
		return new String[] {"float4"};
	}
	
	public Float get(DbRecord rec)
	{
		return (Float)rec.values[ix];
	}
	
}
