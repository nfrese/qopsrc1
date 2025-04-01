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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
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
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
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
import at.qop.qoplib.router.r5pvs.R5PvsClient;
import at.qop.qoplib.router.r5pvs.R5PvsClient.TableResult;
import at.qop.qoplib.router.r5pvs.R5PvsClient.TableResultRow;
import at.qop.qoplib.router.r5pvs.R5PvsClient.TripInfos;
import at.qop.qoplib.router.r5pvs.R5PvsClient.TripLeg;
import at.qop.qoplib.router.r5pvs.R5PvsClient.TripResult;

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

			walk.display = walk.minutes <= THRES;
			bike.display = bike.minutes <= THRES;
			eBike.display = eBike.minutes <= THRES;
			publicTransport.display = publicTransport.minutes > 0 && publicTransport.minutes <= THRES;
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
			@RequestParam(name="cat_id", required = false) List<String> cat,
			@RequestParam(name="analysis_id", required = false) String analysisId,
			@RequestParam(name="provide_data_url", required = false, defaultValue = "false") boolean provideDataUrl,
			@RequestParam(name="routingResultsAsProperties", required = false, defaultValue = "false") boolean routingResultsAsProperties
			
			
		) throws ServletException, IOException, SQLException {
    	boolean isStandort1Analysis = "standort1".equals(analysisId);
		
		Config cfg = checkAuth(username, password);
		boolean enableR5 = enableR5();
		
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
			else if (cat != null && cat.size() > 0) {
				if (cat.contains("without")) {
					sql += " AND cat_id is null";
				}
				else if (cat.contains("with")) {
					sql += " AND cat_id is not null";
				} else {
					sql += " AND cat_id IN ("; 
					int cnt=0;
					for (String ca : cat)
					{
						if (cnt > 0) {
							sql += ", "; 
						}
						sql += escSqlStr(ca);
						cnt++;
					}
					sql += " )"; 
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
			
			if (enableR5) {
				R5PvsClient r5p = new R5PvsClient();
				TableResult tr = r5p.table(null, sources, destinations);
				int r = 0;
				if (tr.rows != null) {
					for (TableResultRow row : tr.rows) {
						time[r][3] = row.minTotalTime/60;
						r++;
					}
				}
			}
			else
			{
				for (int r = 0; r < n; r++) {
					double publicTransportMinutes = time[r][2] * 2;
					publicTransportMinutes = publicTransportMinutes > 7 ? publicTransportMinutes : Double.NaN;

					time[r][3] = publicTransportMinutes;
					r++;
				}
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
				outFeature.routingResults.publicTransport.minutes = time[cnt][3];
				outFeature.routingResults.set();
				
				if (routingResultsAsProperties) {
					outFeature.properties.put("routingResults",outFeature.routingResults);
				}
				if (provideDataUrl) createDataUrl(outFeature);
				
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

	private boolean enableR5() {
		return "true".equalsIgnoreCase(System.getenv("QOP_ENABLE_R5"));
	}

	private String myAddress() {
		return System.getenv("QOP_MY_PUBLIC_ADDRESS");
	}
	
    private void createDataUrl(Feature outFeature) {
    	try {
    		outFeature.properties.remove("url");
			String jsonOut = om().writeValueAsString(outFeature);
			String url = myAddress() + "/qop/rest/api/decode64?dataUrl=" + 
					URLEncoder.encode("data:application/json;base64,"
							+ new String(
									Base64.getEncoder().encode(jsonOut.getBytes("UTF-8")))
							, StandardCharsets.UTF_8.toString())
					;
			outFeature.properties.put("url", url);
		} catch (JsonProcessingException | UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
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
    	boolean enableR5 = enableR5();
    	
		IRouter router = osrm(cfg);
		
		List<SimpleFeature> outFeatures = new ArrayList<>();
		
		ModeEnum[] modes = new ModeEnum[] {ModeEnum.foot, ModeEnum.bike, ModeEnum.car};
    	
		String idStr0 = start_lat + " " +  start_lng + " " + dest_lat + " " + dest_lng;
    	
		LonLat[] points = new LonLat[2];
		points[0] = new LonLat(start_lng, start_lat);
		points[1] = new LonLat(dest_lng, dest_lat);
		
		for (ModeEnum mode : modes)
		{
			if (enableR5 && mode==ModeEnum.car) 
			{ 
				continue;
			}
			
			SimpleFeature routeResult = new SimpleFeature();
			String modName;
			String color;
			switch (mode) {
			case foot : modName="walk"; color="#000000"; break;
			case bike : modName="bike"; color="#00ff00"; break;
			case car : 
				if (!enableR5) 
				{ 
					modName="publicTransport"; color="#ff0000";
				} 
				else {
					modName="car"; color="#ffff00";
				}
			
			break;
			default : modName="unexpected " + mode; color="#a0a0a0";
			}
			
			String idStr = idStr0 +  " " + mode;
			
			routeResult.id=UUID.nameUUIDFromBytes(idStr.getBytes()).toString();
			
			routeResult.properties.put("mode", modName);
			routeResult.properties.put("stroke", color);
			routeResult.properties.put("stroke-width", 3);
			routeResult.properties.put("stroke-opacity", 1);
		

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
		
		if (enableR5) {
			
			R5PvsClient r5p = new R5PvsClient();
			TripResult tr = r5p.route(null, new LonLat[] {points[0]}, new LonLat[] {points[1]});
			
			Map<String, TripInfos> selected = tr.bestTrips.entrySet().stream().filter(x -> x.getKey().equals("BUS") || x.getKey().equals("RAIL")).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			
			int tripNr = 0;
			for (TripInfos sel : selected.values())
			{
				UUID tripId = UUID.nameUUIDFromBytes((idStr0+"_"+tripNr).getBytes());
				
				for (TripLeg leg : sel.tripLegs) {
					SimpleFeature feature2 = new SimpleFeature();
					feature2.id=UUID.nameUUIDFromBytes((idStr0+"_"+leg.legDurationSeconds).getBytes()).toString();
					feature2.properties.put("tripId", tripId+"");
					feature2.properties.put("mode", leg.mode);
					feature2.properties.put("stroke", !("WALK".equals(leg.mode)) ? "#FF0000" : "#000000");
					feature2.properties.put("stroke-width", 3);
					feature2.properties.put("stroke-opacity", 1);
					
					LineString geom;
					try {
						geom = (LineString) new WKTReader(CRSTransform.gfWGS84).read(leg.geom);
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
					JsonNode jo = geomToGeoJson(geom);
					feature2.geometry = jo;
					
					feature2.properties.put("distanceMeters", leg.legDistance);
					feature2.properties.put("durationMinutes", leg.legDurationSeconds / 60);
					feature2.properties.put("routeId", leg.routeId);
					feature2.properties.put("routeShortName", leg.routeShortName);
					feature2.properties.put("routeLongName", leg.routeLongName);
					outFeatures.add(feature2);
				}
				tripNr++;
			}
		}
		
		{
			SimpleFeature feature = new SimpleFeature();
			feature.id=UUID.nameUUIDFromBytes((idStr0+"st").getBytes()).toString();
			feature.properties.put("color", "#808080");
			feature.properties.put("marker-size", "medium");
			feature.properties.put("icon", "dot");
			Point geom = CRSTransform.gfWGS84.createPoint(new Coordinate(start_lng,start_lat));
			JsonNode jo = geomToGeoJson(geom);
			feature.geometry=jo;
			outFeatures.add(feature);
		}
		{
			SimpleFeature feature = new SimpleFeature();
			feature.id=UUID.nameUUIDFromBytes((idStr0+"en").getBytes()).toString();
			feature.properties.put("color", "#00aa00");
			feature.properties.put("marker-size", "medium");
			feature.properties.put("icon", "map-pin");
			Point geom = CRSTransform.gfWGS84.createPoint(new Coordinate(dest_lng,dest_lat));
			JsonNode jo = geomToGeoJson(geom);
			feature.geometry=jo;
			outFeatures.add(feature);
		}
		return returnGeoJson(outFeatures);
    }
    
}
