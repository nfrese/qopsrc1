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

import com.vividsolutions.jts.geom.Envelope;

public class DBUtils {

	public static Envelope parsePGEnvelope(Object obj)
	{
		//BOX(16.1872075686366 48.1212174268439,16.5521039991526 48.3186709198307)
		String str = obj.toString();
		String[] parts = str.split("\\(|\\)");
		String inner = parts[1];
		String[] oords = inner.split(",| ");
		double x1 = Double.valueOf(oords[0]);
		double y1 = Double.valueOf(oords[1]);
		double x2 = Double.valueOf(oords[2]);
		double y2 = Double.valueOf(oords[3]);
		
		return new Envelope(x1,x2,y1,y2);
	}
	

	public static String stMakeEnvelope(Envelope envelope) {
		return "ST_MakeEnvelope(" + envelope.getMinX() + ", " + envelope.getMinY() + ", " 
					+ envelope.getMaxX() + ", " + envelope.getMaxY() +", 4326)";
	}

}
