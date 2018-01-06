package at.qop.qoplib.calculation;

import com.vividsolutions.jts.geom.Geometry;

import at.qop.qoplib.dbbatch.DbRecord;

public class LayerTarget {
	
	Geometry geom;
	DbRecord target;
	double distance;
	
	@Override
	public String toString() {
		return "LayerTarget [geom=" + geom + ", target=" + target + ", distance=" + distance + "]";
	}
}
