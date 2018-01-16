package at.qop.qoplib.calculation.multi;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.calculation.LayerCalculation;
import at.qop.qoplib.calculation.LayerTarget;
import at.qop.qoplib.calculation.MultiTarget;
import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.entities.Analysis;

public class LayerCalculationMultiTT extends LayerCalculation {
	
	private double[][] times;
	private int timesRow;
	private ArrayList<MultiTarget> multiTargets;
	
	public LayerCalculationMultiTT(Point start, Analysis params, double presetWeight, String altRatingFunc,
			DbTable table,
			ArrayList<MultiTarget> multiTargets,
			double[][] times,
			int timesRow) {
		super(start, params, presetWeight, altRatingFunc);
		this.table = table;
		this.multiTargets = multiTargets;
		this.times = times; 
		this.timesRow = timesRow;
	}
	
	@Override
	public void p0loadTargets() {
		orderedTargets = new ArrayList<>();
		for (int t = 0; t < multiTargets.size(); t++)
		{
			MultiTarget mt = multiTargets.get(t);
			
			LayerTarget lt = new LayerTarget();
			
			lt.geom = mt.geom;
			lt.rec = mt.rec;
			orderedTargets.add(lt);
			t++;
		}
	}
	
	public void p2travelTime() {
		if (!params.travelTimeRequired()) throw new RuntimeException("dont!");
		for (int t=0;t<orderedTargets.size();t++)
		{
			LayerTarget lt = orderedTargets.get(t);
			lt.time = times[timesRow][t];
		}
	}

}
