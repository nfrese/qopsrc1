package at.qop.qoplib.calculation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.GLO;
import at.qop.qoplib.calculation.charts.QopBarChart;
import at.qop.qoplib.calculation.charts.QopChart;
import at.qop.qoplib.calculation.charts.QopPieChart;
import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.entities.Analysis;
import at.qop.qoplib.entities.ProfileAnalysis;
import at.qop.qoplib.osrmclient.LonLat;

public abstract class LayerCalculation implements ILayerCalculation {
	
	public final Point start;
	public final ProfileAnalysis params;
	public ProfileAnalysis getParams() {
		return params;
	}

	public final double presetWeight;
	private final String altRatingFunc;
	
	public DbTable table;
	
	public ArrayList<LayerTarget> orderedTargets;
	public ArrayList<LayerTarget> keptTargets = new ArrayList<>();
	
	public double result = Double.NaN;
	public double rating = 1;
	public double weight;
	
	public List<QopChart> charts = null;
	
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
		
		removeDissolved();
		
		if (analysis().travelTimeRequired()) {
			Collections.sort(orderedTargets, (t1, t2) -> Double.compare(t1.time, t2.time));
		} else {
			Collections.sort(orderedTargets, (t1, t2) -> Double.compare(t1.distance, t2.distance));
		}
	}
	
	private void removeDissolved() {
		boolean hasParents = false;
		
		for (LayerTarget lt : orderedTargets)
		{
			if (lt instanceof TargetHasParent)
			{
				hasParents=true;
			}
		}
		if (hasParents)
		{
			ArrayList<LayerTarget> _targetsRemoved = new ArrayList<>();
			HashMap<AbstractLayerTarget, LayerTarget> parentMap = new HashMap<>();
			
			for (LayerTarget lt : orderedTargets)
			{
				
				if (lt instanceof TargetHasParent)
				{
					hasParents=true;
					TargetHasParent ltd = (TargetHasParent)lt;
					LayerTarget prevLt = parentMap.get(ltd.getParent());
					if (prevLt != null)
					{
						if (analysis().travelTimeRequired()) {
							if (lt.time < prevLt.time)
							{
								parentMap.put(ltd.getParent(), lt);
							}
						}
						else
						{
							// not very nice
							if (lt.distance < prevLt.distance)
							{
								parentMap.put(ltd.getParent(), lt);
							}
						}
					}
					else
					{
						parentMap.put(ltd.getParent(), lt);
					}
				}
				else
				{
					_targetsRemoved.add(lt);
				}
			}
			
			for (AbstractLayerTarget parent : new ArrayList<>(parentMap.keySet()))
			{
				if (parent.geom.intersects(start))
				{
					LayerTargetDissolved lt = new LayerTargetDissolved();
					lt.geom = this.start;
					lt.distance = 0.0;
					lt.time = 0.0;
					lt.rec = parent.rec;
					_targetsRemoved.add(lt);
					lt.parent = parent;
					parentMap.put(parent, lt);
				}
			}
			
			_targetsRemoved.addAll(parentMap.values());
			orderedTargets = _targetsRemoved;
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
	
	public QopPieChart addPieChart()
	{
		initChartsList();
		QopPieChart chart_ = new QopPieChart();
		this.charts.add(chart_);
		return chart_;
	}

	public QopBarChart addBarChart()
	{
		initChartsList();
		QopBarChart chart_ = new QopBarChart();
		this.charts.add(chart_);
		return chart_;
	}
	
	private void initChartsList() {
		if (charts == null) charts = new ArrayList<>();
	}
	
	protected static LonLat lonLat(Point p) {
		return new LonLat(p.getCoordinate().x, p.getCoordinate().y);
	}	

	public double getRating()
	{
		return rating;
	}
	
	public double getWeight()
	{
		return weight;
	}
}
