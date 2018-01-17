package at.qop.qoplib.calculation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.GLO;
import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.entities.Analysis;
import at.qop.qoplib.entities.ProfileAnalysis;
import at.qop.qoplib.osrmclient.LonLat;

public abstract class LayerCalculation {
	
	public final Point start;
	public final ProfileAnalysis params;
	public final double presetWeight;
	private final String altRatingFunc;
	
	public DbTable table;
	
	public ArrayList<LayerTarget> orderedTargets;
	public ArrayList<LayerTarget> keptTargets = new ArrayList<>();
	
	public double result;
	public double rating=1;
	public double weight;
	
	public LayerCalculation(Point start, ProfileAnalysis params, double presetWeight, String altRatingFunc) {
		super();
		this.start = start;
		this.params = params;
		this.presetWeight = presetWeight;
		this.weight = presetWeight;
		this.altRatingFunc = altRatingFunc;
	}
	
	public abstract void p0loadTargets();

	public void p1calcDistances() {
		for (LayerTarget lt : orderedTargets)
		{
			lt.distance = CRSTransform.singleton.distanceWGS84(start, lt.geom);
		}
	}
	
	public abstract void p2travelTime();
	
	public void p3OrderTargets() {
		if (analysis().travelTimeRequired()) {
			Collections.sort(orderedTargets, (t1, t2) -> Double.compare(t1.time, t2.time));
		} else {
			Collections.sort(orderedTargets, (t1, t2) -> Double.compare(t1.distance, t2.distance));
		}
	}
	
	public void p4Calculate() {
		
		ScriptContext context = new SimpleScriptContext();
		context.setAttribute("lc", this, ScriptContext.ENGINE_SCOPE);
		
		try {
			if (analysis().analysisfunction != null && analysis().analysisfunction.func != null && !analysis().analysisfunction.func.isEmpty())
			{
				GLO.get().jsEngine.eval(analysis().analysisfunction.func, context);
			}
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
		
		try {
			if (analysis().ratingfunc != null && !analysis().ratingfunc.isEmpty())
			{
				GLO.get().jsEngine.eval(analysis().ratingfunc, context);
			}
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
		
		try {
			if (altRatingFunc != null && !altRatingFunc.isEmpty())
			{
				GLO.get().jsEngine.eval(altRatingFunc, context);
			}
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
					LonLat[] lonLatArr = router.route(analysis().mode, points);
					List<Coordinate> list = Arrays.stream(lonLatArr).map(lonLat -> new Coordinate(lonLat.lon, lonLat.lat)).collect(Collectors.toList());
					target.route = CRSTransform.gfWGS84.createLineString(list.toArray(new Coordinate[list.size()]));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	private boolean routingRequired() {
		return analysis().travelTimeRequired();
	}
	
	public Analysis analysis() {
		return params.analysis;
	}

	public void proto(String line)
	{
		System.out.println(line);
	}
	
	public void keep(LayerTarget target)
	{
		keptTargets.add(target);
	}
	
	protected static LonLat lonLat(Point p) {
		return new LonLat(p.getCoordinate().x, p.getCoordinate().y);
	}	

}
