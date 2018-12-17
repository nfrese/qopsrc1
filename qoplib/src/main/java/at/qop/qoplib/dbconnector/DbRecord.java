/* 
 * Copyright (C) 2018 Norbert Frese
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
*/

package at.qop.qoplib.dbconnector;

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
