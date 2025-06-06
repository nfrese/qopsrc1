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
import java.util.Set;
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
import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.DbTableReader;
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

	private static String colorCar() {
		return "#666699";
	}

	private static String colorPublicTransport() {
		return "#ff0000";
	}

	private static String colorEBike() {
		return "#0390fc";
	}

	private static String colorBike() {
		return "#00ff00";
	}

	private static String colorWalk() {
		return "#000000";
	}
    
    public static class RoutingResults {
		private static final double THRES = 15.;

		public TT walk = new TT();

		public TT bike = new TT();

		public TT eBike = new TT();
		
		public TT publicTransport = new TT();
		
		public TT car = new TT();

		public void set(List<String> modes, boolean important) {
			walk.color=colorWalk();
			bike.color=colorBike();
			eBike.color=colorEBike();
			publicTransport.color=colorPublicTransport();
			car.color = colorCar();
			
			eBike.minutes  = Utils.round(bike.minutes / 1.5,2);
			walk.withinLimit = walk.minutes <= THRES;
			walk.display = walk.withinLimit && walkEnabled(modes);
			
			bike.withinLimit = bike.minutes <= THRES;
			bike.display = bike.withinLimit && bikeEnabled(modes);
			
			eBike.withinLimit = eBike.minutes <= THRES;
			eBike.display = eBike.withinLimit  && eBikeEnabled(modes);
			
			publicTransport.withinLimit = publicTransport.minutes > 0 && publicTransport.minutes <= THRES;
			publicTransport.display = important || (publicTransport.withinLimit && publicTransportEnabled(modes));
		}

		public boolean disp() {
			return walk.display || bike.display  || eBike.display || publicTransport.display || car.display;
		}

    }
    
    public static boolean publicTransportEnabled(List<String> modes) {
		return modes == null || modes.contains("publicTransport");
	}

    public static boolean eBikeEnabled(List<String> modes) {
		return modes == null || modes.contains("eBike");
	}

    public static boolean bikeEnabled(List<String> modes) {
		return modes == null || modes.contains("bike");
	}

    public static boolean walkEnabled(List<String> modes) {
		return modes == null || modes.contains("walk");
	}
    
    public static class TT {
		public double minutes;
		public boolean withinLimit; 
		public boolean display;    
		public String color;
    }
    
    /*
     SELECT * FROM qop.v_pvs_all_improved_1 
   where cat_id='latest' and ((start_timestamp is null and end_timestamp is null)
   or
   ((start_timestamp <= (current_date + interval '2 week'))
   and (end_timestamp is null or (end_timestamp + interval '2 week' >= current_date)))
   )
   order by end_timestamp desc
     */
    
    @GetMapping("/qop/rest/api/traveltime_to_pois")
	protected ResponseEntity<?> traveltime(
			@RequestParam(name="username") String username, 
			@RequestParam(name="password") String password, 
			@RequestParam(name="lat") double start_lat, 
			@RequestParam(name="lng") double start_lng,
			@RequestParam(name="radius_meters", required = false) Double radius,
			@RequestParam(name="poi_table") String[] poiTables,
			@RequestParam(name="cat_id", required = false) List<String> cat,
			@RequestParam(name="modes", required = false) List<String> modes,
			@RequestParam(name="text_filter", required = false) String textFilter,
			@RequestParam(name="time_filter", required = false, defaultValue = "standard") String timeFilter,
			@RequestParam(name="analysis_id", required = false, defaultValue = "main") String analysisId,
			@RequestParam(name="provide_data_url", required = false, defaultValue = "false") boolean provideDataUrl,
			@RequestParam(name="routingResultsAsProperties", required = false, defaultValue = "false") boolean routingResultsAsProperties
			
			
		) throws ServletException, IOException, SQLException {
    	boolean isStandortAnalysis = analysisId != null && !"main".equals(analysisId);
		
		Config cfg = checkAuth(username, password);
		boolean enableR5 = enableR5();
		
		String sqlCat = "SELECT * FROM qop.v_pvs_category_"+ analysisId + "";
		List<SimpleFeature> catF = readInt("qop.pvs_category", sqlCat);
		
		Set<String> catIds = catF.stream().map(c -> (String)c.properties.get("id")).collect(Collectors.toSet());
		
		if (radius == null || radius < 0) {
			radius = 15000.0;
			if ("standort_walkability".equals(analysisId)) {
				radius = 3000.0;
			}
		}
		
		Point start = CRSTransform.gfWGS84.createPoint(new Coordinate(start_lng,start_lat));
		Geometry buffer = CRSTransform.singleton.bufferWGS84Corr(start, radius);
		String geomField ="geom";
		String stIntersectsSql = "(ST_Intersects(" 
				+ geomField + ", 'SRID=4326;" 
				+ buffer + "'::geometry) OR important = true)";

		IRouter router = osrm(cfg);
		
		List<Feature> outFeatures = new ArrayList<>();
		
		for (String poiTable : poiTables) {
			
			DbTableReader reader = new DbTableReader();
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT * FROM " + poiTable + " WHERE " + stIntersectsSql);
			
			switch (timeFilter) {
			case ("standard") :
				sql.append(" AND (start_timestamp is null or start_timestamp <= (current_date + interval '2 week'))");
				sql.append(" AND (end_timestamp is null or (end_timestamp >= current_date))");	
				break;
			case ("more") :
					sql.append(" AND (start_timestamp is null or start_timestamp <= (current_date + interval '2 week'))");
					break;
			case ("all") :
				break;
			}
			
			if (isStandortAnalysis)
			{
				sql.append(" AND cat_id IN (" + catIds.stream().map(c -> "'" + c + "'").collect(Collectors.joining(",")) + ") ");
			}
			else 
			{
				if (cat == null || cat.isEmpty())
				{
					continue;
				}

				if (!cat.contains("nofilter") && cat.size() > 0) {

					if (cat.contains("without")) {
						sql.append(" AND cat_id is null");
					}
					else if (cat.contains("with")) {
						sql.append(" AND cat_id IN (" + catIds.stream().map(c -> "'" + c + "'").collect(Collectors.joining(",")) + ") ");
					} else {
						sql.append(" AND cat_id IN ("); 
						int cnt=0;
						for (String ca : cat)
						{
							if (cnt > 0) {
								sql.append(", "); 
							}
							sql.append(escSqlStr(ca));
							cnt++;
						}
						sql.append(" )"); 
					}
				}
			}
			
			LookupSessionBeans.genericDomain().readTable(
					sql.toString(), reader );
			
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

			ModeEnum[] modesEn = new  ModeEnum[] {ModeEnum.foot, ModeEnum.bike, ModeEnum.car};
			double[][] time = new double[n][4];
			
			try {
				for (int j = 0; j < modesEn.length; j++) {
					double[][] r = router.table(modesEn[j], sources, destinations);
					for (int i = 0; i < n; i++) {
						double timeMinutes = r[0][i] / 60;  // minutes
						time[i][j] = ((double)Math.round(timeMinutes * 100)) / 100;  // round 2 decimal places 
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e); 
			}
			
			if (enableR5) {
				for (int j = 0;j < n;j++)
				{
					time[j][3]=Double.MAX_VALUE;
				}
				
				R5PvsClient r5p = new R5PvsClient();
				TableResult tr = r5p.table(null, sources, destinations);
				int r = 0;
				if (tr != null && tr.rows != null) {
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
						outFeature.geom_ = reader.table.geometryField(colName).get(record);
						
						JsonNode jo = geomToGeoJson(outFeature.geom_);
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
				outFeature.routingResults.set(modes, important(outFeature));
				
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
				.filter(f -> f.containsText(textFilter))
				.sorted((f,g) -> new Double(f.routingResults.bike.minutes).compareTo(g.routingResults.bike.minutes))
				.collect(Collectors.toList());
		
		Map<String, Object> extended = null;
		if (isStandortAnalysis)
		{
			

			Map<String, String> catColorMap = catF.stream().collect(Collectors.toMap(f -> (String)f.properties.get("id"), f -> (String)f.properties.get("color")));
			Map<String, String> catLabelMap = catF.stream().collect(Collectors.toMap(f -> (String)f.properties.get("id"), f -> (String)f.properties.get("label")));

			
			extended = new LinkedHashMap<>();
			
			if (walkEnabled(modes))
			{
				List<Feature> sel = sorted.stream()
						.filter(f -> f instanceof Feature)
						.map(f -> (Feature)f)
						.filter(f -> f.routingResults.walk.withinLimit)
						.collect(Collectors.toList());
				SimpleFeature hullFeature = addConvexHullFeature(sel, "walk", colorWalk());
				sorted.add(hullFeature);
			}
			
			if (bikeEnabled(modes))
			{
				List<Feature> sel = sorted.stream()
						.filter(f -> f instanceof Feature)
						.map(f -> (Feature)f)
						.filter(f -> f.routingResults.bike.withinLimit)
						.collect(Collectors.toList());
				SimpleFeature hullFeature = addConvexHullFeature(sel, "bike", colorBike());
				sorted.add(hullFeature);
			}
			
			if (eBikeEnabled(modes))
			{
				List<Feature> sel = sorted.stream()
						.filter(f -> f instanceof Feature)
						.map(f -> (Feature)f)
						.filter(f -> f.routingResults.eBike.withinLimit)
						.collect(Collectors.toList());
				SimpleFeature hullFeature = addConvexHullFeature(sel, "ebike", colorEBike());
				sorted.add(hullFeature);
			}
			
			if (publicTransportEnabled(modes))
			{
				List<Feature> sel = sorted.stream()
						.filter(f -> f instanceof Feature)
						.map(f -> (Feature)f)
						.filter(f -> f.routingResults.publicTransport.withinLimit)
						.collect(Collectors.toList());
				SimpleFeature hullFeature = addConvexHullFeature(sel, "publicTransport", colorPublicTransport());
				sorted.add(hullFeature);
			}
			
			//extended.put("isochrone15m", jo);
		
			Map<String, Integer> stats = new LinkedHashMap<>();
			for (String catId : catLabelMap.keySet())
			{
				if (!"latest".equals(catId))
				{
					stats.put(catId, new Integer(0));
				}
			}
			
			for (SimpleFeature f : sorted)
			{
				String catId = (String) f.properties.get("cat_id");
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
				freq.cat_id = stat.getKey();
				freq.category = catLabelMap.get(freq.cat_id);
				freq.label = freq.category;
				freq.count = stat.getValue();
				freq.rating = Math.min(freq.count, 10)*1.0/10.0;
				freq.color = catColorMap.get(freq.cat_id);
				
				freqs.add(freq);
				
			}
			
			extended.put("frequencies", freqs);
		}	
		
		System.out.println(sorted.size() + " results");
		return returnGeoJson(sorted, extended );
	}

	private boolean important(Feature f) {
		return Boolean.TRUE.equals(f.properties.get("important"));
	}

	private SimpleFeature addConvexHullFeature(List<Feature> sorted, String mode, String color)
			throws JsonProcessingException, JsonMappingException {
		List<Geometry> collectGeoms = sorted.stream()
				.map(f -> f.geom_)
				.filter(g -> g!=null).collect(Collectors.toList());
		Geometry hull = CRSTransform.singleton.bufferWGS84Corr(Utils.convexHull(collectGeoms, CRSTransform.gfWGS84),200);
		JsonNode jo = geomToGeoJson(hull);

		SimpleFeature hullFeature = new SimpleFeature();
		hullFeature.id = UUID.nameUUIDFromBytes((""+hull).getBytes())+"";
		hullFeature.geometry = jo;
		hullFeature.properties.put("isochrone", "15min");
		hullFeature.properties.put("mode", mode);
		hullFeature.properties.put("stroke", color);
//		hullFeature.properties.put("fill", color);
//		hullFeature.properties.put("transp", 0.5);
		return hullFeature;
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

		public String color;
		public int count;
		public double rating;
		@Deprecated
		public String category;
		public String label;
		public String cat_id;
    	
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
			@RequestParam(name="dest_lng") double dest_lng,
			@RequestParam(name="modes", required = false) List<String> modes
		) throws ServletException, IOException, SQLException {
    
    	Config cfg = checkAuth(username, password);
    	boolean enableR5 = enableR5();
    	
		IRouter router = osrm(cfg);
		
		List<SimpleFeature> outFeatures = new ArrayList<>();
		
		ModeEnum[] modesE = new ModeEnum[] {ModeEnum.foot, ModeEnum.bike, ModeEnum.car};
    	
		String idStr0 = start_lat + " " +  start_lng + " " + dest_lat + " " + dest_lng;
    	
		LonLat[] points = new LonLat[2];
		points[0] = new LonLat(start_lng, start_lat);
		points[1] = new LonLat(dest_lng, dest_lat);
		
		for (ModeEnum mode : modesE)
		{
			if (enableR5 && mode==ModeEnum.car) 
			{ 
				continue;
			}

			SimpleFeature routeResult = new SimpleFeature();
			String modName;
			String color;
			switch (mode) {
			case foot : modName="walk"; color=colorWalk(); break;
			case bike : modName="bike"; color=colorBike(); break;
			case car : 
				if (!enableR5) 
				{ 
					modName="publicTransport"; color=colorPublicTransport();
				} 
				else {
					modName="car"; color=colorCar();
				}

				break;
			default : modName="unexpected " + mode; color="#a0a0a0";
			}

			if (modes != null && !modes.isEmpty() && !modes.contains(modName)
					&& !(modName.equals("bike") && modes.contains("eBike"))) {
				continue;
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


				if (mode == ModeEnum.bike) {
					if (modes.contains("eBike")) {
						SimpleFeature eBikeResult = routeResult.cloneIt();
						eBikeResult.id=UUID.nameUUIDFromBytes((idStr+"eBike").getBytes()).toString();
						eBikeResult.properties.put("mode", "eBike");
						eBikeResult.properties.put("stroke", colorEBike());
						routeResult.properties.put("durationMinutes", result.durationSeconds*.66 / 60);
						outFeatures.add(eBikeResult);
					}
					if (modes.contains("bike")) {
						outFeatures.add(routeResult);
					}
				}
				else
				{
					outFeatures.add(routeResult);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		boolean publicTransportEnabled = modes == null || modes.isEmpty() || modes.contains("publicTransport");
		
		if (enableR5 && publicTransportEnabled) {
			
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
					feature2.properties.put("mode", leg.mode.toLowerCase());
					feature2.properties.put("stroke", !("WALK".equals(leg.mode)) ? colorPublicTransport() : colorWalk());
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
		ResponseEntity<String> re = returnGeoJson(outFeatures);
		return re;
    }
    
}
