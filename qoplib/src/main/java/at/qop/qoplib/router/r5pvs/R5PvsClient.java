package at.qop.qoplib.router.r5pvs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import at.qop.qoplib.calculation.IRouter;
import at.qop.qoplib.entities.ModeEnum;
import at.qop.qoplib.osrmclient.LonLat;
import at.qop.qoplib.osrmclient.OSRMClient;
import at.qop.qoplib.osrmclient.matrix.Arr;
import at.qop.qoplib.osrmclient.matrix.ArrImpl;
import at.qop.qoplib.osrmclient.matrix.ArrView;
import at.qop.qoplib.osrmclient.matrix.DoubleMatrix;
import at.qop.qoplib.osrmclient.matrix.DoubleMatrixImpl;

public class R5PvsClient implements IRouter {


	private String baseUrl(ModeEnum mode) {
		return "http://localhost:5325";
		//return osrmConf.baseUrl(mode);
	}

	@Override
	public double[][] table(ModeEnum mode, LonLat[] sources, LonLat[] destinations) throws IOException {
		throw new RuntimeException("not imlemented");
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


	public void table_(TableResult results, ModeEnum mode, LonLat[] sources, LonLat[] destinations) throws IOException {
		// http://router.project-osrm.org/table/v1/driving/13.388860,52.517037;13.397634,52.529407;13.428555,52.523219?sources=0'

		if (destinations.length == 0)
		{
			return;
		}

		StringBuilder urlSb = new StringBuilder();
		urlSb.append(baseUrl(mode));
		urlSb.append("/single");

		urlSb.append("?sources=");
		String sourcesStr = Arrays.stream(sources).map(p -> p.toString()).collect(Collectors.joining(";"));
		urlSb.append(sourcesStr);
		urlSb.append("&destinations=");
		String targetsStr = Arrays.stream(destinations).map(p -> p.toString()).collect(Collectors.joining(";"));
		urlSb.append(targetsStr);

		long t_start = System.currentTimeMillis();
		//hostPort + "/table/v1/driving/16.369561009437817,48.20423271310815;16.37741831002266,48.20776186641345?sources=0&destinations=1"
		URL url = new URL(urlSb.toString());

		URLConnection con = url.openConnection();

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
			throw new RuntimeException("osrm problem for " + url, ex);
		}
	}

	private static ObjectMapper om = new ObjectMapper();

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

	@Override
	public LonLat[] route(ModeEnum mode, LonLat[] points) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
