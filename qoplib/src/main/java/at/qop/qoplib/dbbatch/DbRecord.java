package at.qop.qoplib.dbbatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

public class DbRecord {
	
	public Object[] values = new Object[0];
	
	public DbRecord() {}
	
	public DbRecord(Object... args) {
		values = args;
		for (int i = 0; i< values.length; i++)
		{
			if (values[i] instanceof Geometry)
			{
				values[i] = WKBWriter.toHex(new WKBWriter().write((Geometry)values[i]));
			}
		}
	}

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

	@Override
	public String toString() {
		return "DbRecord [values=" + Arrays.toString(values) + "]";
	}
}
