package at.qop.qoplib.osrmclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.qop.qoplib.calculation.IRouter;
import at.qop.qoplib.entities.ModeEnum;

public class OSRMClient implements IRouter {
	
	private final String host;
	private int baseport;
	
	public OSRMClient(String host, int baseport) {
		super();
		this.host = host;
		this.baseport = baseport;
	}
	
	@Override
	public double[][] table(ModeEnum mode, LonLat[] sources, LonLat[] destinations) throws IOException {
		// http://router.project-osrm.org/table/v1/driving/13.388860,52.517037;13.397634,52.529407;13.428555,52.523219?sources=0'
		
		if (destinations.length == 0)
		{
			return new double[sources.length][0];
		}
		
		StringBuilder urlSb = new StringBuilder();
		urlSb.append(baseUrl(mode));
		urlSb.append("/table/v1/" + mode.osrmProfile + "/");
		
		String sourcesStr = Arrays.stream(sources).map(p -> p.toString()).collect(Collectors.joining(";"));
		urlSb.append(sourcesStr);
		urlSb.append(';');
		String targetsStr = Arrays.stream(destinations).map(p -> p.toString()).collect(Collectors.joining(";"));
		urlSb.append(targetsStr);
		int cnt = 0;
		urlSb.append("?sources=");
		for (int i=0;i<sources.length;i++)
		{
			if (i>0) urlSb.append(';');
			urlSb.append(cnt++);
		}
		urlSb.append("&destinations=");
		for (int i=0;i<destinations.length;i++)
		{
			if (i>0) urlSb.append(';');
			urlSb.append(cnt++);
		}
		//hostPort + "/table/v1/driving/16.369561009437817,48.20423271310815;16.37741831002266,48.20776186641345?sources=0&destinations=1"
		URL url = new URL(urlSb.toString());
		System.out.println(url);
		
		URLConnection con = url.openConnection();
			
		try (InputStream is= con.getInputStream()) {
			String result = new BufferedReader(new InputStreamReader(is))
					  .lines().collect(Collectors.joining("\n"));
			
			return OSRMClient.parseTableResult(result);
		}
	}

	private String baseUrl(ModeEnum mode) {
		return "http://" + host + ":" + (baseport + mode.osrmPortOffset);
	}
	
	public static double[][] parseTableResult(String json) throws JsonProcessingException, IOException
	{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(json);
		String code = node.get("code").asText();
		if (!"Ok".equalsIgnoreCase(code))
		{
			throw new RuntimeException("osrm return code != ok");
		}
		
		JsonNode durations = node.get("durations");
		int rows = 0;
		int cols = 0;

		{ // count rows and cols
			Iterator<JsonNode> itRows = durations.iterator();
			while (itRows.hasNext()) {
				JsonNode row = itRows.next();
				Iterator<JsonNode> itCols = row.iterator();
				cols=0;
				while (itCols.hasNext()) {
					JsonNode cell = itCols.next();
					cols++;
				}
				rows++;
			}
		}
		
		double[][] durationArr = new double[rows][cols];
		{
			int r = 0;
			
			Iterator<JsonNode> itRows = durations.iterator();
			while (itRows.hasNext()) {
				JsonNode row = itRows.next();
				Iterator<JsonNode> itCols = row.iterator();
				int c=0;
				while (itCols.hasNext()) {
					JsonNode cell = itCols.next();
					durationArr[r][c] = cell.asDouble();
					c++;
				}
				r++;
			}
		}
		return durationArr;
	}

	@Override
	public LonLat[] route(ModeEnum mode, LonLat[] points) throws IOException {
		
		StringBuilder urlSb = new StringBuilder();
		urlSb.append(baseUrl(mode));
		urlSb.append("/route/v1/" + mode.osrmProfile + "/");
		
		String sourcesStr = Arrays.stream(points).map(x -> x.toString()).collect(Collectors.joining(";"));
		urlSb.append(sourcesStr);
		urlSb.append("?geometries=geojson");
		URL url = new URL(urlSb.toString());
		System.out.println(url);
		
		URLConnection con = url.openConnection();
			
		try (InputStream is= con.getInputStream()) {
			String result = new BufferedReader(new InputStreamReader(is))
					  .lines().collect(Collectors.joining("\n"));
			
			return OSRMClient.parseRouteResult(result);
		}
	}

	public static LonLat[] parseRouteResult(String json) throws JsonProcessingException, IOException {
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(json);
		String code = node.get("code").asText();
		if (!"Ok".equalsIgnoreCase(code))
		{
			throw new RuntimeException("osrm return code != ok");
		}
		
		JsonNode coordsNode = node.get("routes").iterator().next().get("geometry").get("coordinates");
		int rows = 0;

		{ // count 
			Iterator<JsonNode> itRows = coordsNode.iterator();
			while (itRows.hasNext()) {
				itRows.next();
				rows++;
			}
		}

		LonLat[] vertices = new LonLat[rows];  
		
		int i=0;
		{ // fill 
			Iterator<JsonNode> itRows = coordsNode.iterator();
			while (itRows.hasNext()) {
				JsonNode loc = itRows.next();
				Iterator<JsonNode> it = loc.iterator();
				double lon = it.next().asDouble();
				double lat = it.next().asDouble();
				vertices[i] = new LonLat(lon, lat); 
				i++;
			}
		}
		return vertices;
	}
	
}
