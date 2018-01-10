package at.qop.qoplib.calculation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.GLO;
import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.dbconnector.fieldtypes.DbGeometryField;
import at.qop.qoplib.entities.Analysis;
import at.qop.qoplib.entities.ModeEnum;
import at.qop.qoplib.entities.ProfileAnalysis;
import at.qop.qoplib.osrmclient.LonLat;

public class LayerCalculation {
	
	public final Point start;
	public final Analysis params;
	
	public DbTable table;
	
	public Collection<DbRecord> targets;
	public ArrayList<LayerTarget> orderedTargets;
	public ArrayList<LayerTarget> keptTargets = new ArrayList<>();
	
	public double result;
	public double rating=1;
	public double presetWeight;
	public double weight;
	
	public LayerCalculation(Point start, Analysis params, double presetWeight) {
		super();
		this.start = start;
		this.params = params;
		this.presetWeight = presetWeight;
		this.weight = presetWeight;
	}
	
	public void p0loadTargets(LayerSource source) {
		Future<LayerCalculationP1Result> future = source.load(start, params);
		try {
			
			LayerCalculationP1Result r = future.get();
			table = r.table;
			targets = r.records;
			
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public void p1calcDistances() {
		DbGeometryField geomField = table.field(params.geomfield, DbGeometryField.class);
		orderedTargets = new ArrayList<>();
		
		for (DbRecord target : targets)
		{
			LayerTarget lt = new LayerTarget();
			
			lt.geom = geomField.get(target);
			lt.distance = CRSTransform.singleton.distanceWGS84(start, lt.geom);
			lt.rec = target;
			orderedTargets.add(lt);
		}
	}
	
	public void p2travelTime(IRouter router) {
		if (travelTimeRequired())
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
					orderedTargets.get(i).time = r[0][i];
				}
			} catch (IOException e) {
				throw new RuntimeException(e); 
			}
		}
	}
	
	public void p3OrderTargets() {
		if (travelTimeRequired()) {
			Collections.sort(orderedTargets, (t1, t2) -> Double.compare(t1.time, t2.time));
		} else {
			Collections.sort(orderedTargets, (t1, t2) -> Double.compare(t1.distance, t2.distance));
		}
	}
	
	private boolean travelTimeRequired() {
		return params.mode != null && params.mode != ModeEnum.air;
	}
	
	public void p4Calculate() {
		
		ScriptContext context = new SimpleScriptContext();
		context.setAttribute("lc", this, ScriptContext.ENGINE_SCOPE);
		
		try {
			GLO.get().jsEngine.eval(params.evalfn, context);
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public void p5route(IRouter router) {
		if (routingRequired())
		{
			for (LayerTarget target : keptTargets)
			{
				LonLat[] points = new LonLat[2];
				points[0] = lonLat(start);
				Coordinate c = target.geom.getCoordinate();
				points[1] = new LonLat(c.x, c.y);
				try {
					LonLat[] lonLatArr = router.route(params.mode, points);
					List<Coordinate> list = Arrays.stream(lonLatArr).map(lonLat -> new Coordinate(lonLat.lon, lonLat.lat)).collect(Collectors.toList());
					target.route = CRSTransform.gfWGS84.createLineString(list.toArray(new Coordinate[list.size()]));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	private boolean routingRequired() {
		return travelTimeRequired();
	}

	public void proto(String line)
	{
		System.out.println(line);
	}
	
	public void keep(LayerTarget target)
	{
		keptTargets.add(target);
	}
	
	private static LonLat lonLat(Point p) {
		return new LonLat(p.getCoordinate().x, p.getCoordinate().y);
	}	

}
