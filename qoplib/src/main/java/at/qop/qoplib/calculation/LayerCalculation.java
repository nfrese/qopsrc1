package at.qop.qoplib.calculation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.GLO;
import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.dbconnector.fieldtypes.DbGeometryField;
import at.qop.qoplib.entities.Analysis;

public class LayerCalculation {
	
	public final Point start;
	public final Analysis params;
	
	public DbTable table;
	
	public Collection<DbRecord> targets;
	public ArrayList<LayerTarget> orderedTargets;
	public ArrayList<LayerTarget> keptTargets = new ArrayList<>();
	
	public double result;
	
	public LayerCalculation(Point start, Analysis params) {
		super();
		this.start = start;
		this.params = params;
	}
	
	public void p1loadTargets(LayerSource source) {
		Future<LayerCalculationP1Result> future = source.load(start, params);
		try {
			
			LayerCalculationP1Result r = future.get();
			table = r.table;
			targets = r.records;
			
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void p2OrderTargets() {
		
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
		
		Collections.sort(orderedTargets, (t1, t2) -> Double.compare(t1.distance, t2.distance));
	}
	
	public void p3Calculate() {
		
		ScriptContext context = new SimpleScriptContext();
		context.setAttribute("lc", this, ScriptContext.ENGINE_SCOPE);
		
		try {
			GLO.get().jsEngine.eval(params.evalfn, context);
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public void proto(String line)
	{
		System.out.println(line);
	}
	
	public void keep(LayerTarget target)
	{
		keptTargets.add(target);
	}

}
