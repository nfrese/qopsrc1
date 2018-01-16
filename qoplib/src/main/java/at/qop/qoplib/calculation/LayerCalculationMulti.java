package at.qop.qoplib.calculation;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.entities.Analysis;

public class LayerCalculationMulti extends LayerCalculation {
	
	public LayerCalculationMulti(Point start, Analysis params, double presetWeight, String altRatingFunc,
			DbTable table,
			ArrayList<LayerTarget> orderedTargets) {
		super(start, params, presetWeight, altRatingFunc);
		this.table = table;
		this.orderedTargets = orderedTargets;
	}
	
	@Override
	public void p0loadTargets() {
	}
	
	public void p2travelTime() {
//		if (travelTimeRequired())
//		{
//			LonLat[] sources = new LonLat[1];
//			sources[0] = lonLat(start); 
//
//			LonLat[] destinations = new LonLat[orderedTargets.size()];
//			int i = 0;
//			for (i = 0; i < this.orderedTargets.size(); i++)
//			{
//				Coordinate c = orderedTargets.get(i).geom.getCoordinate();
//				destinations[i] = new LonLat(c.x, c.y);
//			}
//
//			try {
//				double[][] r = router.table(params.mode, sources, destinations);
//				for (i = 0; i < this.orderedTargets.size(); i++) {
//					double timeMinutes = r[0][i] / 60;  // minutes
//					orderedTargets.get(i).time = ((double)Math.round(timeMinutes * 100)) / 100;  // round 2 decimal places 
//				}
//			} catch (IOException e) {
//				throw new RuntimeException(e); 
//			}
//		}
	}

}
