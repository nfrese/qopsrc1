package at.qop.qoplib.dbbatch;

import java.util.ArrayList;
import java.util.Collection;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

public class DbRecord {
	
	public Object[] values = new Object[0];

	public Collection<Geometry> getGeometries(DbTable table)
	{
		Collection<Geometry> results = new ArrayList<>();
	
		for (int i = 0; i < table.colNames.length; i++)
		{
	
			if ("geometry".equals(table.typeNames[i]))
			{
				WKBReader wkbReader = new WKBReader();  
				try {
					Geometry geom = wkbReader.read(WKBReader.hexToBytes(String.valueOf(values[i])));
					results.add(geom);
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return results;
	}
	
}
