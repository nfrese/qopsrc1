package at.qop.qoplib.dbbatch.fieldtypes;

import at.qop.qoplib.dbbatch.DbRecord;

public class DbTextField extends DbFieldAbstract {

	public String[] expectedTypeName() {
		return new String[] {"varchar", "text"};
	}
	
	public String get(DbRecord rec)
	{
		return (String)rec.values[ix];
	}
	
}
