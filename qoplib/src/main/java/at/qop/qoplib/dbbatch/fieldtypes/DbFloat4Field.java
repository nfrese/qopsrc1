package at.qop.qoplib.dbbatch.fieldtypes;

import at.qop.qoplib.dbbatch.DbRecord;

public class DbFloat4Field extends DbFieldAbstract {

	public String[] expectedTypeName() {
		return new String[] {"float4"};
	}
	
	public Float get(DbRecord rec)
	{
		return (Float)rec.values[ix];
	}
	
}
