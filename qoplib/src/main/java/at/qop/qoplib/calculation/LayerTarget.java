package at.qop.qoplib.calculation;

import com.vividsolutions.jts.geom.LineString;

public class LayerTarget extends AbstractLayerTarget {
	
	public double distance;
	public double time;
	
	// calculated in JavaScript
	public String caption;
	public double value;
	public String unit;
	public LineString route;
	
	@Override
	public String toString() {
		return "LayerTarget [geom=" + geom + ", target=" + rec + ", distance=" + distance + "]";
	}
	
}
