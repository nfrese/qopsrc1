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

package at.qop.ws;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.geojson.GeoJsonWriter;

import at.qop.qoplib.Config;
import at.qop.qoplib.Constants;
import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.calculation.CRSTransform;
import at.qop.qoplib.calculation.IRouter;
import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.DbTableReader;
import at.qop.qoplib.dbconnector.fieldtypes.DbInt4Field;
import at.qop.qoplib.entities.ModeEnum;
import at.qop.qoplib.osrmclient.LonLat;
import at.qop.qoplib.osrmclient.OSRMClient;

// http://localhost:8080/qop/rest/api/traveltime_to_pois?poi_table=qop.osm_pois&lon=48.4526522&lat=16.6000785&radius_meters=500&username=api&password=3243628746982

@RestController
public class QOPRestApiRoute extends QOPRestApiBase {
       
	private ObjectMapper om = new ObjectMapper();
	
    public QOPRestApiRoute() {
        super();
    }

    @GetMapping("/qop/rest/api/traveltime_to_pois")
	protected ResponseEntity<?> traveltime(
			@RequestParam(name="username") String username, 
			@RequestParam(name="password") String password, 
			@RequestParam(name="lat") double start_lat, 
			@RequestParam(name="lon") double start_lon,
			@RequestParam(name="radius_meters") double radius,
			@RequestParam(name="poi_table") String[] poiTables
		) throws ServletException, IOException, SQLException {
		
		Config cfg = checkAuth(username, password);
		
		Point start = CRSTransform.gfWGS84.createPoint(new Coordinate(start_lat,start_lon));
		Geometry buffer = CRSTransform.singleton.bufferWGS84Corr(start, radius);
		String geomField ="geom";
		String stIntersectsSql = "ST_Intersects(" +geomField + ", 'SRID=4326;" + buffer + "'::geometry)";

		IRouter router = new OSRMClient(cfg.getOSRMConf(), Constants.SPLIT_DESTINATIONS_AT);
		
		Map<String,Object> outRoot = new LinkedHashMap<>();
		List<Object> outFeatures = new ArrayList<>();
		outRoot.put("features", outFeatures);
		
		for (String poiTable : poiTables) {
			
			DbTableReader reader = new DbTableReader();
			LookupSessionBeans.genericDomain().readTable("SELECT * FROM " + poiTable + " WHERE " + stIntersectsSql, reader );
			
			DbInt4Field gid = reader.table.int4Field("gid");
			
			LonLat[] sources = new LonLat[1];
			sources[0] = new LonLat(start.getX(), start.getY());

			int n = reader.records.size();
			LonLat[] destinations = new LonLat[n];
			for (int i = 0; i < n; i++)
			{
				DbRecord record = reader.records.get(i);
				Point targetPoint = (Point)reader.table.geometryField(geomField).get(record);
				destinations[i] = new LonLat(targetPoint.getX(), targetPoint.getY());
			}

			double[] time = new double[n];
			
			try {
				double[][] r = router.table(ModeEnum.foot, sources, destinations);
				for (int i = 0; i < n; i++) {
					double timeMinutes = r[0][i] / 60;  // minutes
					time[i] = ((double)Math.round(timeMinutes * 100)) / 100;  // round 2 decimal places 
				}
			} catch (IOException e) {
				throw new RuntimeException(e); 
			}
			
			int cnt =0;
			for (DbRecord record : reader.records)
			{
				Map<String,Object> outFeature = new LinkedHashMap<>();
				
				Map<String,Object> outProperties = new LinkedHashMap<>();
				outFeature.put("id", poiTable + ":" + gid.get(record));
				outFeature.put("properties", outProperties);
				for (int i = 0; i < reader.table.colNames.length;i++) {
					String colName = reader.table.colNames[i];
					if (reader.table.typeNames[i].equals("geometry"))
					{
						Geometry value = reader.table.geometryField(colName).get(record);
						GeoJsonWriter gw = new GeoJsonWriter();
						String json = gw.write(value);
						JsonNode jo = om.readTree(json);
						if (geomField.equals(colName))
						{
							outProperties.put(colName, jo);
						}
						else
						{
							outFeature.put("geometry", jo);
						}
					}
					else if (reader.table.typeNames[i].equals("jsonb"))
					{
						String json = String.valueOf(record.values[i]);
						outProperties.put(colName, om.readTree(json));
					}
					else
					{
						Object value = record.values[i];
						outProperties.put(colName, value);
					}
				}
				
				Map<String,Object> outRouting = new LinkedHashMap<>();
				outRouting.put("walkMinutes", time[cnt]);
				outProperties.put("routingResults", outRouting);
				
				outFeatures.add(outFeature);
				cnt++;
			}
		}
		
		
		String jsonOut = om.writeValueAsString(outRoot);


		return ResponseEntity.ok().header("Content-Type", "application/json;charset=UTF-8").body(jsonOut);
	}



}
