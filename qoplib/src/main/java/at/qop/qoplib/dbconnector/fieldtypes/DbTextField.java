package at.qop.qoplib.dbconnector.fieldtypes;

import at.qop.qoplib.dbconnector.DbRecord;

public class DbTextField extends DbFieldAbstract {

	public String[] expectedTypeName() {
		return new String[] {"varchar", "text"};
	}
	
	public String get(DbRecord rec)
	{
		return (String)rec.values[ix];
	}
	
}
