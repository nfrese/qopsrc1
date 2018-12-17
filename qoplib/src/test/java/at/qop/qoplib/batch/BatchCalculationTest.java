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

package at.qop.qoplib.batch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTReader;

import at.qop.qoplib.Constants;
import at.qop.qoplib.batch.WriteBatTable.BatRecord;
import at.qop.qoplib.calculation.ILayerCalculationP1Params;
import at.qop.qoplib.calculation.LayerCalculationP1Result;
import at.qop.qoplib.calculation.LayerSource;
import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Analysis;
import at.qop.qoplib.entities.AnalysisFunction;
import at.qop.qoplib.entities.ModeEnum;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileAnalysis;
import at.qop.qoplib.osrmclient.OSRMClient;
import at.qop.qoplib.osrmclient.OSRMClientTest;
import org.junit.Assert;

public class BatchCalculationTest {
	
	@Test
	public void test() throws ParseException
	{
		List<Address> input = new ArrayList<>();
		
		{
			Address adr = new Address();
			adr.name = "Rauhensteingasse 10";
			adr.geom = (Point) new WKTReader().read("POINT (16.3724265546418 48.2061121370655)");
			input.add(adr);
			
		}
		{
			Address adr = new Address();
			adr.name = "Albertinaplatz 2";
			adr.geom = (Point) new WKTReader().read("POINT (16.3695610097329 48.2042327131692)");
			input.add(adr);
		}
		{
			Address adr = new Address();
			adr.name = "Neuer Markt 8";
			adr.geom = (Point) new WKTReader().read("POINT (16.3704315013128 48.2053297142949)");
			input.add(adr);
		}
		{
			Address adr = new Address();
			adr.name = "Plankengasse 3";
			adr.geom = (Point) new WKTReader().read("POINT (16.3699659807455 48.2063095697309)");
			input.add(adr);
		}
		{
			Address adr = new Address();
			adr.name = "Liebenberggasse 7/3";
			adr.geom = (Point) new WKTReader().read("POINT (16.3784976304613 48.2060303886915)");
			input.add(adr);
		}
		{
			Address adr = new Address();
			adr.name = "Liebenberggasse 7/2";
			adr.geom = (Point) new WKTReader().read("POINT (16.3782822940926 48.2059552422407)");
			input.add(adr);
		}
		{
			Address adr = new Address();
			adr.name = "Liebenberggasse 7/1";
			adr.geom = (Point) new WKTReader().read("POINT (16.3784724170685 48.2058753275844)");
			input.add(adr);
		}

		Profile currentProfile = createProfile();
		
		BatchCalculationInMemory bc = initBC(input, currentProfile);
		bc.run();
		
		List<BatRecord> output = bc.getOutput();
		
		BatRecord r1 = output.stream().filter(r -> r.name.equals("Rauhensteingasse 10")).findFirst().get();
		Assert.assertEquals(2.96, r1.colGrps[0].result, 0.1);
		
		System.out.println(output);
		
	}

	protected Profile createProfile() {
		Profile currentProfile = new Profile();
		currentProfile.name = "Wohnen";
		
		
		ProfileAnalysis pa = new ProfileAnalysis();
		pa.category = "cat1";
		pa.categorytitle = "Catergory1";
		Analysis analysis = new Analysis();
		analysis.analysisfunction  = new AnalysisFunction();
		analysis.analysisfunction.func = "var cnt=0;\n" + 
				"var result=0;\n" + 
				"var descriptionField=lc.table.textField('description');\n" + 
				"for each (var target in lc.orderedTargets) {\n" + 
				"        lc.result = target.time;\n" + 
				"        var description = descriptionField.get(target.rec);\n" + 
				"        target.caption = \"<b>\" + description + \"</b>\" \n" + 
				"               + \"<br>Wegzeit Minuten: \" +  lc.result;\n" + 
				"        lc.keep(target);\n" + 
				"        break;\n" + 
				"};";
		analysis.mode = ModeEnum.foot;
		analysis.geomfield = "GEOM";
		analysis.radius = 2000;
		analysis.name = "A1";
		
		pa.analysis = analysis;
		
		currentProfile.profileAnalysis = Arrays.asList(pa);
		return currentProfile;
	}

	protected BatchCalculationInMemory initBC(List<Address> input, Profile currentProfile) {
		return new BatchCalculationInMemory(currentProfile, input) {

			@Override
			protected OSRMClient initRouter() {
				return new OSRMClient(OSRMClientTest.OSRM_SERVER, 5000, Constants.SPLIT_DESTINATIONS_AT);
			}

			@Override
			protected LayerSource initSource() {

				return new LayerSource() {

					@Override
					public LayerCalculationP1Result load(Geometry start, ILayerCalculationP1Params layerParams) {

						LayerCalculationP1Result r = new LayerCalculationP1Result();
						r.table = new DbTable();
						r.table.colNames = new String[] {"GEOM", "description"};
						r.table.sqlTypes = new int[] {java.sql.Types.VARCHAR, java.sql.Types.VARCHAR};
						r.table.typeNames = new String[] {"geometry", "text"};
						r.records = new ArrayList<>();
						{
							DbRecord rec = new DbRecord();
							try {
								Point point = (Point) new WKTReader().read("POINT (16.3699659807455 48.2063095697309)");
								String wkb = WKBWriter.toHex(new WKBWriter().write(point));
								rec.values = new Object[] {wkb , "Ziel1"};
								r.records.add(rec);
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
						return r;
					}
				};
			}

		};
	}

}
