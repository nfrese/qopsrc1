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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.ServletException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.geojson.GeoJsonWriter;

import at.qop.qoplib.Config;
import at.qop.qoplib.Constants;
import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.Utils;
import at.qop.qoplib.calculation.CRSTransform;
import at.qop.qoplib.calculation.IRouter;
import at.qop.qoplib.calculation.LayerTarget;
import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.DbTableReader;
import at.qop.qoplib.dbconnector.fieldtypes.DbInt4Field;
import at.qop.qoplib.dbconnector.fieldtypes.DbTextField;
import at.qop.qoplib.entities.ModeEnum;
import at.qop.qoplib.osrmclient.LonLat;
import at.qop.qoplib.osrmclient.OSRMClient;
import at.qop.qoplib.osrmclient.RouteResult;

@RestController
public class QOPRestApiRoute extends QOPRestApiBase {
       
    public QOPRestApiRoute() {
        super();
    }

    public static class RoutingResults {
		private static final double THRES = 15.;

		public TT walk = new TT();

		public TT bike = new TT();

		public TT eBike = new TT();
		
		public TT publicTransport = new TT();
		
		public TT car = new TT();

		public void set() {
			
			eBike.minutes  = bike.minutes / 1.5;
			publicTransport.minutes = car.minutes * 2;
			publicTransport.minutes = publicTransport.minutes > 7 ? publicTransport.minutes : Double.NaN;

			walk.display = walk.minutes <= THRES;
			bike.display = bike.minutes <= THRES;
			eBike.display = eBike.minutes <= THRES;
			publicTransport.display = publicTransport.minutes <= THRES;
		}

		public boolean disp() {
			return walk.display || bike.display  || eBike.display || publicTransport.display || car.display;
		}
    	
    }
    
    public static class TT {
		public double minutes;
		public boolean display;    	
    }
    
    @GetMapping("/qop/rest/api/traveltime_to_pois")
	protected ResponseEntity<?> traveltime(
			@RequestParam(name="username") String username, 
			@RequestParam(name="password") String password, 
			@RequestParam(name="lat") double start_lat, 
			@RequestParam(name="lng") double start_lng,
			@RequestParam(name="radius_meters") double radius,
			@RequestParam(name="poi_table") String[] poiTables,
			@RequestParam(name="cat_id", required = false) String cat,
			@RequestParam(name="analysis_id", required = false) String analysisId
			
		) throws ServletException, IOException, SQLException {
    	boolean isStandort1Analysis = "standort1".equals(analysisId);
		
		Config cfg = checkAuth(username, password);
		
		Point start = CRSTransform.gfWGS84.createPoint(new Coordinate(start_lng,start_lat));
		Geometry buffer = CRSTransform.singleton.bufferWGS84Corr(start, radius);
		String geomField ="geom";
		String stIntersectsSql = "ST_Intersects(" +geomField + ", 'SRID=4326;" + buffer + "'::geometry)";

		IRouter router = osrm(cfg);
		
		List<Geometry> collectGeoms = new ArrayList<>();
		List<Feature> outFeatures = new ArrayList<>();
		
		for (String poiTable : poiTables) {
			
			DbTableReader reader = new DbTableReader();
			String sql = "SELECT * FROM " + poiTable + " WHERE " + stIntersectsSql;
			if (isStandort1Analysis)
			{
				sql += " AND cat_id is not null and cat_id != 'latest'";
			}
			else if (cat != null) {
				if (cat.equals("without")) {
					sql += " AND cat_id is null";
				}
				else if (cat.equals("with")) {
					sql += " AND cat_id is not null";
				} else {
					sql += " AND cat_id = " + escSqlStr(cat);
				}
			}
			
			LookupSessionBeans.genericDomain().readTable(
					sql, reader );
			
			DbTextField fid = reader.table.textField("fid");
		
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

			ModeEnum[] modes = new  ModeEnum[] {ModeEnum.foot, ModeEnum.bike, ModeEnum.car};
			double[][] time = new double[n][4];
			
			try {
				for (int j = 0; j < modes.length; j++) {
					double[][] r = router.table(modes[j], sources, destinations);
					for (int i = 0; i < n; i++) {
						double timeMinutes = r[0][i] / 60;  // minutes
						time[i][j] = ((double)Math.round(timeMinutes * 100)) / 100;  // round 2 decimal places 
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e); 
			}
			
			int cnt =0;
			for (DbRecord record : reader.records)
			{
				Feature outFeature = new Feature();
				
				outFeature.id = fid.get(record);
				for (int i = 0; i < reader.table.colNames.length;i++) {
					String colName = reader.table.colNames[i];
					if ("fid".equals(colName))
					{
						continue;
					}
					
					if (reader.table.typeNames[i].equals("geometry"))
					{
						Geometry value = reader.table.geometryField(colName).get(record);
						if (value != null && isStandort1Analysis) { 
							collectGeoms.add(value);
						}
						
						JsonNode jo = geomToGeoJson(value);
						if (geomField.equals(colName))
						{
							outFeature.geometry= jo;
						}
						else
						{
							outFeature.properties.put(colName, jo);
						}
					}
					else if (reader.table.typeNames[i].equals("jsonb"))
					{
						String json = String.valueOf(record.values[i]);
						outFeature.properties.put(colName, om().readTree(json));
					}
					else
					{
						Object value = record.values[i];
						outFeature.properties.put(colName, value);
					}
				}
				
				outFeature.routingResults.walk.minutes = time[cnt][0];
				outFeature.routingResults.bike.minutes = time[cnt][1];
				outFeature.routingResults.car.minutes = time[cnt][2];
				outFeature.routingResults.set();
				
				outFeatures.add(outFeature);
				cnt++;
			}
		}
		
		List<SimpleFeature> sorted = outFeatures.stream()
				.filter(f -> f.routingResults.disp())
				.sorted((f,g) -> new Double(f.routingResults.bike.minutes).compareTo(g.routingResults.bike.minutes))
				.collect(Collectors.toList());
		
		Map<String, Object> extended = null;
		if (isStandort1Analysis)
		{
			extended = new LinkedHashMap<>();
			
			Geometry hull = CRSTransform.singleton.bufferWGS84Corr(Utils.convexHull(collectGeoms, CRSTransform.gfWGS84),200);
			JsonNode jo = geomToGeoJson(hull);
			
			SimpleFeature hullFeature = new SimpleFeature();
			hullFeature.id = UUID.nameUUIDFromBytes((""+hull).getBytes())+"";
			hullFeature.geometry = jo;
			hullFeature.properties.put("isochrone", "15min");
			sorted.add(hullFeature);
			
			//extended.put("isochrone15m", jo);
		
			Map<String, Integer> stats = new LinkedHashMap<>();
			for (SimpleFeature f : sorted)
			{
				String catId = (String) f.properties.get("cat_label");
				if (catId != null)
				{
					Integer stat = stats.get(catId);
					if (stat == null)
					{
						stat = 0;
					}
					stat++;
					stats.put(catId, stat);
				}
			}
			
			List<FrequencyItem> freqs = new ArrayList<>();
			
			for (Entry<String, Integer> stat : stats.entrySet()) {
				
				FrequencyItem freq = new FrequencyItem();
				freq.category = stat.getKey();
				freq.count = stat.getValue();
				
				freqs.add(freq);
				
			}
			
			extended.put("frequencies", freqs);
		}	
		
		return returnGeoJson(sorted, extended );
	}

    public static class FrequencyItem {

		public Integer count;
		public String category;
    	
    }
    
	private JsonNode geomToGeoJson(Geometry value) throws JsonProcessingException, JsonMappingException {
		GeoJsonWriter gw = new GeoJsonWriter();
		String json = gw.write(value);
		JsonNode jo = om().readTree(json);
		return jo;
	}

	private OSRMClient osrm(Config cfg) {
		return new OSRMClient(cfg.getOSRMConf(), Constants.SPLIT_DESTINATIONS_AT);
	}

    @GetMapping("/qop/rest/api/route")
	protected ResponseEntity<?> route(
			@RequestParam(name="username") String username, 
			@RequestParam(name="password") String password, 
			@RequestParam(name="lat") double start_lat, 
			@RequestParam(name="lng") double start_lng,
			@RequestParam(name="dest_lat") double dest_lat, 
			@RequestParam(name="dest_lng") double dest_lng
		) throws ServletException, IOException, SQLException {
    
    	Config cfg = checkAuth(username, password);
    	
		IRouter router = osrm(cfg);
		
		List<SimpleFeature> outFeatures = new ArrayList<>();
		
		ModeEnum[] modes = new ModeEnum[] {ModeEnum.foot, ModeEnum.bike, ModeEnum.car};
    	
		String idStr0 = start_lat + " " +  start_lng + " " + dest_lat + " " + dest_lng;
    	
		for (ModeEnum mode : modes)
		{
			SimpleFeature routeResult = new SimpleFeature();
			String modName;
			String color;
			switch (mode) {
			case foot : modName="walk"; color="000000"; break;
			case bike : modName="bike"; color="#00ff00"; break;
			case car : modName="publicTransport"; color="#ff0000"; break;
			default : modName="unexpected " + mode; color="#a0a0a0";
			}
			
			String idStr = idStr0 +  " " + mode;
			
			routeResult.id=UUID.nameUUIDFromBytes(idStr.getBytes()).toString();
			
			routeResult.properties.put("mode", modName);
			routeResult.properties.put("stroke", color);
			routeResult.properties.put("stroke-width", 3);
			routeResult.properties.put("stroke-opacity", 1);
		
			LonLat[] points = new LonLat[2];
			points[0] = new LonLat(start_lng, start_lat);
			points[1] = new LonLat(dest_lng, dest_lat);
			try {
				RouteResult result = router.route(mode, points);
				List<Coordinate> list = Arrays.stream(result.vertices).map(lonLat -> new Coordinate(lonLat.lon, lonLat.lat)).collect(Collectors.toList());
				LineString geom = CRSTransform.gfWGS84.createLineString(list.toArray(new Coordinate[list.size()]));
				JsonNode jo = geomToGeoJson(geom);
				routeResult.geometry = jo;
				
				routeResult.properties.put("distanceMeters", result.distanceMeters);
				routeResult.properties.put("durationMinutes", result.durationSeconds / 60);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			outFeatures.add(routeResult);
		}
		
		{
			SimpleFeature feature = new SimpleFeature();
			feature.id=UUID.nameUUIDFromBytes((idStr0+"st").getBytes()).toString();
			feature.properties.put("marker-color", "#808080");
			feature.properties.put("marker-size", "medium");
			feature.properties.put("marker-symbol", "circle");
			Point geom = CRSTransform.gfWGS84.createPoint(new Coordinate(start_lng,start_lat));
			JsonNode jo = geomToGeoJson(geom);
			feature.geometry=jo;
			outFeatures.add(feature);
		}
		{
			SimpleFeature feature = new SimpleFeature();
			feature.id=UUID.nameUUIDFromBytes((idStr0+"en").getBytes()).toString();
			feature.properties.put("marker-color", "#00aa00");
			feature.properties.put("marker-size", "medium");
			feature.properties.put("marker-symbol", "circle");
			Point geom = CRSTransform.gfWGS84.createPoint(new Coordinate(dest_lng,dest_lat));
			JsonNode jo = geomToGeoJson(geom);
			feature.geometry=jo;
			outFeatures.add(feature);
		}
		return returnGeoJson(outFeatures);
    }
    
}
