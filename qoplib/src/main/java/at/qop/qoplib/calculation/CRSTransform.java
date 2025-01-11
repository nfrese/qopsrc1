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

package at.qop.qoplib.calculation;

import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;

public class CRSTransform {

	public static GeometryFactory gfWGS84 = new GeometryFactory(new PrecisionModel(), 4326);
	public static GeometryFactory gf31256 = new GeometryFactory(new PrecisionModel(), 31256);
	
	public static CRSTransform singleton = new CRSTransform();
	
	private MathTransform transform4326to31256;
	private MathTransform transform31256to4326;
	
	{
		try {
			CoordinateReferenceSystem sourceCRS = CRS.parseWKT("GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]");;
			CoordinateReferenceSystem targetCRS = CRS.parseWKT("PROJCS[\"MGI / Austria GK East\",GEOGCS[\"MGI\",DATUM[\"Militar_Geographische_Institute\",SPHEROID[\"Bessel 1841\",6377397.155,299.1528128,AUTHORITY[\"EPSG\",\"7004\"]],TOWGS84[577.326,90.129,463.919,5.137,1.474,5.297,2.4232],AUTHORITY[\"EPSG\",\"6312\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9108\"]],AUTHORITY[\"EPSG\",\"4312\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",0],PARAMETER[\"central_meridian\",16.33333333333333],PARAMETER[\"scale_factor\",1],PARAMETER[\"false_easting\",0],PARAMETER[\"false_northing\",-5000000],AUTHORITY[\"EPSG\",\"31256\"],AXIS[\"Y\",EAST],AXIS[\"X\",NORTH]]");
			transform4326to31256 = CRS.findMathTransform(sourceCRS, targetCRS, true);
			transform31256to4326 = CRS.findMathTransform(targetCRS, sourceCRS, true);
		} catch (FactoryException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Geometry fromWGS84(Geometry sourceGeometry)
	{
		try {
			Geometry g0 = JTS.transform( sourceGeometry, transform4326to31256);
			return gf31256.createGeometry(g0);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public Geometry toWGS84(Geometry sourceGeometry)
	{
		try {
			Geometry g0 = JTS.transform( sourceGeometry, transform31256to4326);
			return gfWGS84.createGeometry(g0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public double distance(Geometry g1, Geometry g2)
	{
		if (g1.getSRID() == 4326 && g2.getSRID() == 4326)
		{
			return distanceWGS84(g1, g2);
		}
		else
		{
			return g1.distance(g2);
		}
	}

	public double distanceWGS84(Geometry g1, Geometry g2) {
		return fromWGS84(g1).distance(fromWGS84(g2));
	}
	
	public Geometry bufferWGS84Corr(Geometry g1, double meter) {
		return bufferWGS84(g1, meter, 1.02); // add 2% for the possible error of 8 segments per quadrant
	}
	
	public Geometry bufferWGS84(Geometry g1, double meter, double corrFact) {
		Geometry pGeom = fromWGS84(g1);
		Geometry buffered = pGeom.buffer(meter * corrFact);
		return toWGS84(buffered);
	}

	public double lenWGS84(LineSegment lseg) {
		LineString geometry = lseg.toGeometry(gfWGS84);
		return distanceWGS84(geometry.getStartPoint(), geometry.getEndPoint()); 
	}
	
}
