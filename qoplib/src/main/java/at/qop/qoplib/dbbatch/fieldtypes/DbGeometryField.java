package at.qop.qoplib.dbbatch.fieldtypes;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

import at.qop.qoplib.dbbatch.DbRecord;

public class DbGeometryField extends DbFieldAbstract {

	private static WKBReader wkbReader = new WKBReader();
	
	public String[] expectedTypeName() {
		return new String[] {"geometry"};
	}
	
	public Geometry get(DbRecord rec)
	{
		try {
			Geometry geom = wkbReader.read(WKBReader.hexToBytes(String.valueOf(rec.values[ix])));
			return geom;
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
}
