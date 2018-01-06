package at.qop.qoplib.calculation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.dbbatch.DbRecord;
import at.qop.qoplib.dbbatch.DbTable;
import at.qop.qoplib.entities.LayerParams;
import at.qop.qoplib.osrmclient.OSRMClientTest;

public class LayerCalculationTest {

	@Test
	public void test() {
		
		LayerParams params = new LayerParams();
		params.geomfield = "shape";
		
		LayerCalculation lc = new LayerCalculation(
				CRSTransform.gfWGS84.createPoint(new Coordinate(16.37242655454094,48.2061121366474)),
				params);
		
		LayerSource source = new LayerSource() {

			@Override
			public Future<LayerCalculationP1Result> load(Point start, LayerParams layerParams) {
				Callable<LayerCalculationP1Result> callable = new Callable<LayerCalculationP1Result>() {

					@Override
					public LayerCalculationP1Result call() throws Exception {
						LayerCalculationP1Result r = new LayerCalculationP1Result();
						r.table = new DbTable();
						r.table.colNames = new String[] { "shape", "value" };
						r.table.typeNames = new String[] { "geometry", "double" };
						
						r.targets = new ArrayList<>();
						
						Random rand = new Random(666);
						
						Arrays.stream(OSRMClientTest.demoData(-1)).forEach(
								d -> r.targets.add(
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
		lc.p1loadTargets(source);
		lc.p2OrderTargets();
		
		System.out.println();
		
	}

}
