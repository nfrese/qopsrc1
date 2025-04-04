package at.qop.qoplib.router.r5pvs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import at.qop.qoplib.calculation.IRouter;
import at.qop.qoplib.entities.ModeEnum;
import at.qop.qoplib.osrmclient.LonLat;
import at.qop.qoplib.osrmclient.OSRMClient;
import at.qop.qoplib.osrmclient.RouteResult;
import at.qop.qoplib.osrmclient.matrix.Arr;
import at.qop.qoplib.osrmclient.matrix.ArrImpl;
import at.qop.qoplib.osrmclient.matrix.ArrView;
import at.qop.qoplib.osrmclient.matrix.DoubleMatrix;
import at.qop.qoplib.osrmclient.matrix.DoubleMatrixImpl;
import at.qop.qoplib.router.r5pvs.R5PvsClient.TableResult;

public class R5PvsClient {

	private static ObjectMapper om = new ObjectMapper();

	public static class TripLeg {
		public String routeId;
		public String routeLongName;
		public String routeShortName;
		public int routeType;
		public int boardStopId;
		public int alightStopId;
		public String mode;
		public int legDistance;
		public int legDurationSeconds;
		public String boardStopName;
		public String alightStopName;
		public String geom;
	}

	public static class TripInfos {
		public String mode;
		public String routeId;
		public String routeLongName;
		public String routeShortName;
		public double totalDurationSeconds=Double.MAX_VALUE;
		public String accessMode;
		public int accessTime;
		public String egressMode;
		public int egressTime;
		public int departureTime;
		public double rideTimesSeconds;

		public List<TripLeg> tripLegs = new ArrayList<>();
	}
	
	public static class TripResult {
		public List<TripInfos> trips;
		public Map<String, TripInfos> bestTrips;
	}
	
	private String baseUrl(ModeEnum mode) {
		String baseUrl = System.getenv("QOP_R5_BASEURL");
		if (baseUrl != null)
		{
			return baseUrl;
		}
		else
		{
			return "http://localhost:5325";
		}
	}

	public static class TableResult {
		public List<TableResultRow> rows = new ArrayList<>();
		public Map<Integer, TableResultRouteInfo> routeInfos = new TreeMap<>();
	}

	public static class TableResultRow {

		public double minTotalTime;
		public TableResultRoute bestRoute;
		public List<TableResultRoute> routes = new ArrayList<>();

	}

	public static class TableResultRoute {

		public int routeId;
		public int count;
		public double minDuration;

	}


	public TableResult table(ModeEnum mode, LonLat[] sources, LonLat[] destinations) throws IOException {

		if (destinations.length == 0)
		{
			return null;
		}

		R5PvsClient.TableResult results = new TableResult();
		
		StringBuilder urlSb = new StringBuilder();
		urlSb.append(baseUrl(mode));
		urlSb.append("/single");


		long t_start = System.currentTimeMillis();
		URL url = new URL(urlSb.toString());

		URLConnection con = url.openConnection();
		con.setDoOutput(true);

	    OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());

	    ObjectNode rn = om.createObjectNode();
	    rn.set("sources", toArrayNode(sources));
	    rn.set("destinations", toArrayNode(destinations));
	    
	    writer.write(rn+"");
	    writer.flush();

		try (InputStream is= con.getInputStream()) {
			long t_callFinished = System.currentTimeMillis();

			parseTableResult(results, new BufferedReader(new InputStreamReader(is)));
			long t_finished = System.currentTimeMillis();

			System.out.println(sources.length + "x" + destinations.length 
					+ " t_call=" + (t_callFinished - t_start) 
					+ "ms t_parse="+ (t_finished - t_callFinished) + "ms " + url);
			
		}
		catch (Exception ex)
		{
			throw new RuntimeException("r5 problem for " + url, ex);
		}
		return results;
	}

	private ArrayNode toArrayNode(LonLat[] sources) {
		ArrayNode an = om.createArrayNode();
	    for ( LonLat ll : sources) {
	    	ArrayNode an2 = om.createArrayNode();
	    	an2.add(ll.lon);
	    	an2.add(ll.lat);
	    	an.add(an2);
	    }
		return an;
	}

	public static void parseTableResult(TableResult tr, Reader jsonReader) throws JsonProcessingException, IOException
	{

		JsonNode jn = om.readTree(jsonReader);

		for (JsonNode n : jn.at("/results")) {

			TableResultRow row = new TableResultRow();
			row.minTotalTime = n.at("/minTotalTime").asDouble();
			row.bestRoute = parseRoute(n.at("/bestRoute"));
			for (JsonNode rn : n.at("/routes")) {

				TableResultRoute route = parseRoute(rn);
				row.routes.add(route);
			}
			tr.rows.add(row);
		}

		for (JsonNode n : jn.at("/routeInfos")) {
			TableResultRouteInfo ri = parseRouteInfo(n);
			tr.routeInfos.put(ri.routeId, ri);
		}
	}

	public TripResult route(ModeEnum mode, LonLat[] sources, LonLat[] destinations) throws IOException {

		if (destinations.length == 0)
		{
			return null;
		}

		StringBuilder urlSb = new StringBuilder();
		urlSb.append(baseUrl(mode));
		urlSb.append("/plan2");

		urlSb.append("?sources=");
		String sourcesStr = Arrays.stream(sources).map(p -> p.toString()).collect(Collectors.joining(";"));
		urlSb.append(sourcesStr);
		urlSb.append("&destinations=");
		String targetsStr = Arrays.stream(destinations).map(p -> p.toString()).collect(Collectors.joining(";"));
		urlSb.append(targetsStr);

		long t_start = System.currentTimeMillis();
		URL url = new URL(urlSb.toString());

		URLConnection con = url.openConnection();

		try (InputStream is= con.getInputStream()) {
			long t_callFinished = System.currentTimeMillis();
			System.out.println(sources.length + "x" + destinations.length 
					+ " t_call=" + (t_callFinished - t_start) 
				    + "ms " + url);
			TripResult tr = om.readValue(is, TripResult.class);
			return tr;

		}
		catch (Exception ex)
		{
			throw new RuntimeException("r5 problem for " + url, ex);
		}
	}
	
	
	private static TableResultRoute parseRoute(JsonNode jsonNode) throws IOException {
		if (jsonNode.isMissingNode())
		{
			return null;
		}
		
		TableResultRoute trRoute = new TableResultRoute();

		trRoute.routeId = jsonNode.at("/routeId").asInt();
		trRoute.minDuration = jsonNode.at("/minDuration").asDouble();
		trRoute.count = jsonNode.at("/routeId").asInt();

		return trRoute;
	}

	public static class TableResultRouteInfo {

		public int routeId;
		public String routeName;
		public String routeLongName;
		public int routeType;

	}

	private static TableResultRouteInfo parseRouteInfo(JsonNode n) throws IOException {
		TableResultRouteInfo trRoute = new TableResultRouteInfo();

		trRoute.routeId = n.at("/routeId").asInt();
		trRoute.routeName = n.at("/routeName").asText();
		trRoute.routeLongName = n.at("/routeLongName").asText();

		trRoute.routeType = n.at("/routeType").asInt();
		return trRoute;
	}

}
