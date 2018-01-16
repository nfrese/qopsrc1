package at.qop.qoplib.calculation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.fieldtypes.DbGeometryField;
import at.qop.qoplib.entities.Analysis;
import at.qop.qoplib.osrmclient.LonLat;

public class LayerCalculationSingle extends LayerCalculation {

	private final LayerSource source;
	private final IRouter router;
	public Collection<DbRecord> targets;
	
	public LayerCalculationSingle(Point start, Analysis params, double presetWeight, String altRatingFunc,
			LayerSource source, IRouter router) {
		super(start, params, presetWeight, altRatingFunc);
		this.source = source;
		this.router = router;
	}
	
	@Override
	public void p0loadTargets() {
		LayerCalculationP1Result r = source.load(start, params);
		table = r.table;
		targets = r.records;
		
		DbGeometryField geomField = table.field(params.geomfield, DbGeometryField.class);

		ArrayList<LayerTarget> targets_ = new ArrayList<>();
		for (DbRecord target : targets)
		{
			LayerTarget lt = new LayerTarget();
			
			lt.geom = geomField.get(target);
			
			lt.rec = target;
			targets_.add(lt);
		}
		
		orderedTargets = targets_;
	}
	
	public void p2travelTime() {
		if (params.travelTimeRequired())
		{
			LonLat[] sources = new LonLat[1];
			sources[0] = lonLat(start); 

			LonLat[] destinations = new LonLat[orderedTargets.size()];
			int i = 0;
			for (i = 0; i < this.orderedTargets.size(); i++)
			{
				Coordinate c = orderedTargets.get(i).geom.getCoordinate();
				destinations[i] = new LonLat(c.x, c.y);
			}

			try {
				double[][] r = router.table(params.mode, sources, destinations);
				for (i = 0; i < this.orderedTargets.size(); i++) {
					double timeMinutes = r[0][i] / 60;  // minutes
					orderedTargets.get(i).time = ((double)Math.round(timeMinutes * 100)) / 100;  // round 2 decimal places 
				}
			} catch (IOException e) {
				throw new RuntimeException(e); 
			}
		}
	}

}
