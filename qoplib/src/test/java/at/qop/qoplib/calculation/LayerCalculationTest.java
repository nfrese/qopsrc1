/* 
 * Copyright (C) 2018 Norbert Frese
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
*/

package at.qop.qoplib.calculation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;

import org.junit.Assert;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Analysis;
import at.qop.qoplib.entities.AnalysisFunction;
import at.qop.qoplib.entities.ProfileAnalysis;
import at.qop.qoplib.osrmclient.OSRMClientTest;

public class LayerCalculationTest {

	@Test
	public void test() {

		ProfileAnalysis pa = new ProfileAnalysis();
		Analysis analysis = new Analysis();
		pa.analysis = analysis;
		analysis.geomfield = "shape";

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
			analysis.analysisfunction = analysisFunction;
		}
		{
			StringJoiner sj = new StringJoiner("\n");
			sj.add("lc.rating = lc.result * 10");
			analysis.ratingfunc = sj.toString();
		}

		LayerSource source = new LayerSource() {

			@Override
			public LayerCalculationP1Result load(Geometry start, List<Address> addresses, ILayerCalculationP1Params layerParams) {

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
		LayerCalculation lc = new LayerCalculationSingle(
				CRSTransform.gfWGS84.createPoint(new Coordinate(16.37242655454094,48.2061121366474)),
				pa, 1, null, source, null);		
		lc.p0loadTargets();
		lc.p1calcDistances();
		lc.p2travelTime();
		lc.p3OrderTargets();
		lc.p4Calculate();

		Assert.assertEquals(1.86511, lc.result, 0.01);
		Assert.assertEquals(18.6511, lc.rating, 0.01);

	}

}
