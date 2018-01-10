package at.qop.qoplib.calculation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.entities.Analysis;
import at.qop.qoplib.entities.AnalysisFunction;
import at.qop.qoplib.osrmclient.OSRMClientTest;
import org.junit.Assert;

public class LayerCalculationTest {

	@Test
	public void test() {

		Analysis params = new Analysis();
		params.geomfield = "shape";

		{
			StringJoiner sj = new StringJoiner("\n");
			sj.add("var cnt=0;");
			sj.add("var result=0;");
			sj.add("var valueField=lc.table.doubleField('value');");
			sj.add("for each (var target in lc.orderedTargets) {"); 
			sj.add("        if (cnt >= 5) break;"); 
			sj.add("        lc.proto(target.toString());");
			sj.add("        var value = valueField.get(target.rec);");
			sj.add("        lc.proto(value);");
			sj.add("        result += value;");
			sj.add("        lc.keep(target);");
			sj.add("        cnt++;");
			sj.add("};");
			sj.add("lc.result = result");
			sj.add("lc.proto('sum=' + result);");

			AnalysisFunction analysisFunction = new AnalysisFunction(); 
			analysisFunction.func = sj.toString(); 
			params.analysisfunction = analysisFunction;
		}
		{
			StringJoiner sj = new StringJoiner("\n");
			sj.add("lc.rating = lc.result * 10");
			params.ratingfunc = sj.toString();
		}

		LayerCalculation lc = new LayerCalculation(
				CRSTransform.gfWGS84.createPoint(new Coordinate(16.37242655454094,48.2061121366474)),
				params, 1, null);

		LayerSource source = new LayerSource() {

			@Override
			public Future<LayerCalculationP1Result> load(Point start, ILayerCalculationP1Params layerParams) {
				Callable<LayerCalculationP1Result> callable = new Callable<LayerCalculationP1Result>() {

					@Override
					public LayerCalculationP1Result call() throws Exception {
						LayerCalculationP1Result r = new LayerCalculationP1Result();
						r.table = new DbTable();
						r.table.colNames = new String[] { "shape", "value" };
						r.table.typeNames = new String[] { "geometry", "double" };

						r.records = new ArrayList<>();

						Random rand = new Random(666);

						Arrays.stream(OSRMClientTest.demoData(-1)).forEach(
								d -> r.records.add(
										new DbRecord(
												CRSTransform.gfWGS84.createPoint(
														new Coordinate(d.lon, d.lat)), 
												(double)rand.nextFloat())));
						return r;
					}

				};
				FutureTask<LayerCalculationP1Result> ft = new FutureTask<LayerCalculationP1Result>(callable);
				ft.run();
				return ft;
			}
		};
		lc.p0loadTargets(source);
		lc.p1calcDistances();
		lc.p2travelTime(null);
		lc.p3OrderTargets();
		lc.p4Calculate();

		Assert.assertEquals(1.86511, lc.result, 0.01);
		Assert.assertEquals(18.6511, lc.rating, 0.01);

	}

}
