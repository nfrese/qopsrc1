package at.qop.qoplib.calculation;

import com.vividsolutions.jts.geom.Geometry;

import at.qop.qoplib.dbbatch.DbRecord;

public class LayerTarget {
	
	public Geometry geom;
	public DbRecord rec;
	public double distance;
	public boolean keep = false;
	
	@Override
	public String toString() {
		return "LayerTarget [geom=" + geom + ", target=" + rec + ", distance=" + distance + "]";
	}
}
