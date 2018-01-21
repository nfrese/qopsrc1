package at.qop.qoplib.calculation;

import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.dbconnector.DbRecord;

public class CreateTargetsSingle extends CreateTargets<LayerTarget> {

	protected LayerTarget createParent(DbRecord rec, Geometry shape) {
		LayerTarget parentLt = new LayerTarget();
		parentLt.geom = shape; 
		parentLt.rec = rec;
		return parentLt;
	}
	
	protected void addTarget(List<LayerTarget> results, DbRecord rec, Geometry shape) {
		LayerTarget lt = new LayerTarget();
		
		lt.geom = shape; 
		lt.rec = rec;
		results.add(lt);
	}
	
	protected void addTargetDissolved(List<LayerTarget> results, LayerTarget parentLt, DbRecord rec,
			Point point) {
		LayerTargetDissolved lt = new LayerTargetDissolved();
		lt.parent = parentLt;
		lt.geom = point; 
		lt.rec = rec;
		results.add(lt);
	}
}
