package at.qop.qoplib.calculation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.fieldtypes.DbGeometryField;
import at.qop.qoplib.entities.ProfileAnalysis;
import at.qop.qoplib.osrmclient.LonLat;

public class LayerCalculationSingle extends LayerCalculation {

	private static final CreateTargetsSingle CREATE_TARGETS = new CreateTargetsSingle();
	
	private final LayerSource source;
	private final IRouter router;
	
	public LayerCalculationSingle(Point start, ProfileAnalysis params, double presetWeight, String altRatingFunc,
			LayerSource source, IRouter router) {
		super(start, params, presetWeight, altRatingFunc);
		this.source = source;
		this.router = router;
	}
	
	@Override
	public void p0loadTargets() {
		Collection<DbRecord> records;
		LayerCalculationP1Result r = source.load(start, analysis());
		table = r.table;
		records = r.records;
		
		DbGeometryField geomField = table.field(analysis().geomfield, DbGeometryField.class);

		ArrayList<LayerTarget> targets_ = new ArrayList<>();
		for (DbRecord rec : records)
		{
			Geometry shape = geomField.get(rec);
			CREATE_TARGETS.createTargetsFromRecord(targets_, rec, shape);
		}
		
		orderedTargets = targets_;
	}

	public void p2travelTime() {
		if (analysis().travelTimeRequired())
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
				double[][] r = router.table(analysis().mode, sources, destinations);
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
