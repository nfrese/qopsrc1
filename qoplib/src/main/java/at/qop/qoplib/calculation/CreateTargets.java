package at.qop.qoplib.calculation;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import at.qop.qoplib.dbconnector.DbRecord;

public abstract class CreateTargets <T extends AbstractLayerTarget>
{
	public static double TARGET_EVERY_X_METERS = 25.0;
	
	public void createTargetsFromRecord(List<T> results, DbRecord rec, Geometry shape) {
		
		if (shape instanceof Point)
		{
			fromPoint(results, rec, (Point)shape);
		}
		else if (shape instanceof MultiPoint)
		{
			fromMultiPoint(results, rec, (MultiPoint)shape);
			
		} 
		else if (shape instanceof LineString)
		{
			T parentLt = createParent(rec, shape);
			fromLineString(results, parentLt, rec, (LineString) shape, true);
		}
		else if (shape instanceof MultiLineString)
		{
			T parentLt = createParent(rec, shape);
			MultiLineString mls = (MultiLineString)shape;
			for (int i=0;i<mls.getNumGeometries();i++)
			{
				LineString lis = (LineString) mls.getGeometryN(i);
				fromLineString(results, parentLt, rec, lis, true);
			}
		}
		else if (shape instanceof Polygon)
		{
			T parentLt = createParent(rec, shape);
			Polygon polygon = (Polygon) shape;
			fromPolygon(results, parentLt, rec, polygon);
		}		
		else if (shape instanceof MultiPolygon)
		{
			T parentLt = createParent(rec, shape);
			MultiPolygon mpy = (MultiPolygon)shape;
			for (int i=0;i<mpy.getNumGeometries();i++)
			{
				Polygon polygon = (Polygon) mpy.getGeometryN(i);
				fromPolygon(results, parentLt, rec, polygon);
			}
		}
		else
		{
			throw new RuntimeException("no support for geometry type " + shape);
		}
	}

	private void fromPolygon(List<T> results, T parentLt, DbRecord rec,
			Polygon polygon) {
		fromLineString(results, parentLt, rec, polygon.getExteriorRing(), false);
		for (int i = 0; i < polygon.getNumInteriorRing(); i++)
		{
			fromLineString(results, parentLt, rec, polygon.getInteriorRingN(i), false);
		}
	}

	public void fromMultiPoint(List<T> results, DbRecord rec, MultiPoint mp) {
		T parentLt = createParent(rec, mp);
		
		for (int i=0; i< mp.getNumPoints(); i++)
		{
			Point point = (Point)mp.getGeometryN(i);
			addTargetDissolved(results, parentLt, rec, point);
		}
	}

	public void fromPoint(List<T> results, DbRecord rec, Point shape) {
		addTarget(results, rec, shape);
	}

	public void fromLineString(List<T> results, T parentLt, DbRecord rec, LineString geom, boolean addLast) {
		
		for (int i=0; i< geom.getNumPoints(); i++)
		{
			Point point = CRSTransform.gfWGS84.createPoint(geom.getCoordinateN(i));
			addTargetDissolved(results, parentLt, rec, point);
			
			if (i < geom.getNumPoints() - 1)
			{
				LineSegment lseg = new LineSegment(geom.getCoordinateN(i), geom.getCoordinateN(i+1));
				fromLineSegment(results, parentLt, rec, lseg);
			}
		}
	}

	public void fromLineSegment(List<T> results, T parentLt, DbRecord rec, LineSegment lseg) {
		
		double len = CRSTransform.singleton.lenWGS84(lseg);
		
		int n = (int) Math.floor(len/TARGET_EVERY_X_METERS);
		
		for (int i=0; i< n; i++)
		{
			Coordinate coord = lseg.pointAlong(1/(n+1));
			
			Point point = CRSTransform.gfWGS84.createPoint(coord);
			addTargetDissolved(results, parentLt, rec, point);
		}
	}

	protected abstract T createParent(DbRecord rec, Geometry shape);
	
	protected abstract void addTarget(List<T> results, DbRecord rec, Geometry shape);
	
	protected abstract void addTargetDissolved(List<T> results, T parentLt, DbRecord rec,
			Point point);

}
