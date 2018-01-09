package at.qop.qoplib.calculation;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

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
			
		} catch (MismatchedDimensionException | TransformException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Geometry toWGS84(Geometry sourceGeometry)
	{
		try {
			Geometry g0 = JTS.transform( sourceGeometry, transform31256to4326);
			return gfWGS84.createGeometry(g0);
		} catch (MismatchedDimensionException | TransformException e) {
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
	
	public Geometry bufferWGS84(Geometry g1, double meter) {
		Geometry pGeom = fromWGS84(g1);
		Geometry buffered = pGeom.buffer(meter);
		return toWGS84(buffered);
	}
	
}
