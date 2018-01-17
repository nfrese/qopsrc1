package at.qop.qoplib.calculation.multi;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.calculation.LayerCalculation;
import at.qop.qoplib.calculation.LayerTarget;
import at.qop.qoplib.calculation.MultiTarget;
import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.entities.Analysis;
import at.qop.qoplib.entities.ProfileAnalysis;

public class LayerCalculationMultiSimple extends LayerCalculation {
	
	private ArrayList<MultiTarget> multiTargets;
	
	public LayerCalculationMultiSimple(Point start, ProfileAnalysis params, double presetWeight, String altRatingFunc,
			DbTable table, ArrayList<MultiTarget> multiTargets) {
		super(start, params, presetWeight, altRatingFunc);
		this.table = table;
		this.multiTargets = multiTargets;
	}

	@Override
	public void p0loadTargets() {
		orderedTargets = new ArrayList<>();
		for (MultiTarget mt : multiTargets)
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
