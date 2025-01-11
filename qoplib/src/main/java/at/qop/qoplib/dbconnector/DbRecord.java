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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

import at.qop.qoplib.dbconnector.fieldtypes.DbDoubleField;

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
			} else 
			if ("rid".equals(table.colNames[i]))
			{
				Integer width = table.int4Field("width").get(this);					
				Integer height = table.int4Field("height").get(this);	
				Double x1 = table.doubleField("upperleftx").get(this);					
				Double y1 = table.doubleField("upperlefty").get(this);
				Double scalex = table.doubleField("scalex").get(this);					
				Double scaley = table.doubleField("scaley").get(this);				
				
				double x2 = x1 + width *  scalex;
				double y2 = y1 + height *  scaley;
				
				Coordinate c1 = new Coordinate(x1,y1);
				Coordinate c2 = new Coordinate(x2,y1);
				Coordinate c3 = new Coordinate(x2,y2);
				Coordinate c4 = new Coordinate(x1,y2);
				Coordinate c5 = new Coordinate(x1,y1);
				
				Geometry geom = new GeometryFactory().createPolygon(new Coordinate[] {c1, c2, c3, c4, c5});
				results.add(geom);
			}
		}
		return results;
	}

	@Override
	public String toString() {
		return "DbRecord [values=" + Arrays.toString(values) + "]";
	}
}
