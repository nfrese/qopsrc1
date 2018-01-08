package at.qop.qoplib.dbconnector.fieldtypes;

import java.math.BigDecimal;

import at.qop.qoplib.dbconnector.DbRecord;

public class DbDoubleField extends DbFieldAbstract {

	public String[] expectedTypeName() {
		return new String[] { "float8", "double", "numeric" };
	}
	
	public Double get(DbRecord rec)
	{
		if (rec.values[ix] instanceof BigDecimal)
		{
			BigDecimal bd = (BigDecimal)rec.values[ix];
			return bd.doubleValue();
		}
		
		return (Double)rec.values[ix];
	}
	
}
