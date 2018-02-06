package at.qop.qoplib.calculation.multi;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.strtree.STRtree;

import at.qop.qoplib.calculation.CRSTransform;
import at.qop.qoplib.calculation.LayerCalculation;
import at.qop.qoplib.calculation.LayerTarget;
import at.qop.qoplib.calculation.MultiTarget;
import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.entities.ProfileAnalysis;

public class LayerCalculationMultiEuclidean extends LayerCalculation {
	
	private STRtree spatIx;
	
	public LayerCalculationMultiEuclidean(Point start, ProfileAnalysis params, double presetWeight, String altRatingFunc,
			DbTable table, STRtree spatIx) {
		super(start, params, presetWeight, altRatingFunc);
		this.table = table;
		this.spatIx = spatIx;
	}

	@Override
	public void p0loadTargets() {
		orderedTargets = new ArrayList<>();
		if (!analysis().hasRadius()) throw new RuntimeException("dont!");
		Geometry buffer = CRSTransform.singleton.bufferWGS84(start, analysis().getRadius());
		
		@SuppressWarnings("unchecked")
		List<MultiTarget> results = spatIx.query(buffer.getEnvelopeInternal());
		
		for (MultiTarget mt : results)
		{
			LayerTarget lt = new LayerTarget();
			
			lt.geom = mt.geom;
			lt.rec = mt.rec;
			orderedTargets.add(lt);
		}
	}
	
	public void p2travelTime() {
	}

}
