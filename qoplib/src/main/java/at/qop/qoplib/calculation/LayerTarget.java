package at.qop.qoplib.calculation;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

import at.qop.qoplib.dbconnector.DbRecord;

public class LayerTarget {
	
	public Geometry geom;
	public DbRecord rec;
	public double distance;
	public double time;
	
	// calculated in Javascript
	public String caption;
	public double value;
	public String unit;
	public LineString route;
	
	@Override
	public String toString() {
		return "LayerTarget [geom=" + geom + ", target=" + rec + ", distance=" + distance + "]";
	}
}
