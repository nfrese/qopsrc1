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

package at.qop.qoplib.dbconnector.fieldtypes;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTReader;

import at.qop.qoplib.dbconnector.DbRecord;

public class DbGeometryField extends DbFieldAbstract {

	private static WKBReader wkbReader = new WKBReader();
	
	
	public String[] expectedTypeName() {
		return new String[] {"geometry","\"public\".\"geometry\""};
	}
	
	public Geometry get(DbRecord rec)
	{
		Object obj = rec.values[ix];
		String str = String.valueOf(obj);
		
		if (str.startsWith("SRID")) {
			try {
				
				String[] split = str.replaceAll("^SRID=", "").split(";");
				WKTReader wktReader = new WKTReader(new GeometryFactory(new PrecisionModel(),Integer.valueOf( split[0])));
				
				return wktReader.read(split[1]);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		
		try {
			Geometry geom = wkbReader.read(WKBReader.hexToBytes(str));
			return geom;
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
}
