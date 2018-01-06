package at.qop.qoplib.calculation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.dbbatch.DbRecord;
import at.qop.qoplib.dbbatch.DbTable;
import at.qop.qoplib.dbbatch.fieldtypes.DbGeometryField;
import at.qop.qoplib.entities.LayerParams;

public class LayerCalculation {
	
	public final Point start;
	public final LayerParams layerParams;
	
	public DbTable table;
	
	ArrayList<DbRecord> targets;
	ArrayList<LayerTarget> orderedTargets;
	
	public LayerCalculation(Point start, LayerParams layerParams) {
		super();
		this.start = start;
		this.layerParams = layerParams;
	}
	
	public void p1loadTargets(LayerSource source) {
		Future<LayerCalculationP1Result> future = source.load(start, layerParams);
		try {
			
			LayerCalculationP1Result r = future.get();
			table = r.table;
			targets = r.targets;
			
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void p2OrderTargets() {
		
		DbGeometryField geomField = table.findField(layerParams.geomfield, DbGeometryField.class);
		orderedTargets = new ArrayList<>();
		
		for (DbRecord target :targets)
		{
			LayerTarget lt = new LayerTarget();
			
			lt.geom = geomField.get(target);
			lt.distance = CRSTransform.singleton.distanceWGS84(start, lt.geom);
			lt.target = target;
			orderedTargets.add(lt);
		}
		
		Collections.sort(orderedTargets, (t1, t2) -> Double.compare(t1.distance, t2.distance));
	}

}
