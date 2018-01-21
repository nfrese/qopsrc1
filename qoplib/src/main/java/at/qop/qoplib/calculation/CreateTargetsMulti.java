package at.qop.qoplib.calculation;

import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.dbconnector.DbRecord;

public class CreateTargetsMulti extends CreateTargets<MultiTarget> {

	protected MultiTarget createParent(DbRecord rec, Geometry shape) {
		MultiTarget parentLt = new MultiTarget();
		parentLt.geom = shape; 
		parentLt.rec = rec;
		return parentLt;
	}
	
	protected void addTarget(List<MultiTarget> results, DbRecord rec, Geometry shape) {
		MultiTarget lt = new MultiTarget();
		
		lt.geom = shape; 
		lt.rec = rec;
		results.add(lt);
	}
	
	protected void addTargetDissolved(List<MultiTarget> results, MultiTarget parentLt, DbRecord rec,
			Point point) {
		MultiTargetDissolved lt = new MultiTargetDissolved();
		lt.parent = parentLt;
		lt.geom = point; 
		lt.rec = rec;
		results.add(lt);
	}
}
