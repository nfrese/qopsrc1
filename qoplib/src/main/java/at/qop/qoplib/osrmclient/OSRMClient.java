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

package at.qop.qoplib.osrmclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.qop.qoplib.calculation.IRouter;
import at.qop.qoplib.entities.ModeEnum;
import at.qop.qoplib.osrmclient.matrix.Arr;
import at.qop.qoplib.osrmclient.matrix.ArrImpl;
import at.qop.qoplib.osrmclient.matrix.ArrView;
import at.qop.qoplib.osrmclient.matrix.DoubleMatrix;
import at.qop.qoplib.osrmclient.matrix.DoubleMatrixImpl;

public class OSRMClient implements IRouter {
	
	private final int splitDestinationsAt;
	private final String host;
	private int baseport;
	
	public OSRMClient(String host, int baseport, int splitDestinationsAt) {
		super();
		this.host = host;
		this.baseport = baseport;
		this.splitDestinationsAt = splitDestinationsAt;
	}

	@Override
	public double[][] table(ModeEnum mode, LonLat[] sources, LonLat[] destinations) throws IOException {
		
		int rows = sources.length; int cols = destinations.length;

		ArrImpl<LonLat> destinationsArr = new ArrImpl<>(destinations);
		
		DoubleMatrixImpl results = new DoubleMatrixImpl(rows, cols);
		
		for (ArrView<LonLat> destView : destinationsArr.views(splitDestinationsAt))
		{
			DoubleMatrix resultsView = results.createView(0, rows, destView.getStart(), destView.length());
			
			table_(resultsView, mode, sources, destView);
		}
		
		return results.arr();
	}
	
	
	public void table_(DoubleMatrix results, ModeEnum mode, LonLat[] sources, Arr<LonLat> destinations) throws IOException {
		// http://router.project-osrm.org/table/v1/driving/13.388860,52.517037;13.397634,52.529407;13.428555,52.523219?sources=0'
		
		if (destinations.length() == 0)
		{
			return;
		}
		
		StringBuilder urlSb = new StringBuilder();
		urlSb.append(baseUrl(mode));
		urlSb.append("/table/v1/" + mode.osrmProfile + "/");
		
		String sourcesStr = Arrays.stream(sources).map(p -> p.toString()).collect(Collectors.joining(";"));
		urlSb.append(sourcesStr);
		urlSb.append(';');
		String targetsStr = destinations.stream().map(p -> p.toString()).collect(Collectors.joining(";"));
		urlSb.append(targetsStr);
		int cnt = 0;
		urlSb.append("?sources=");
		for (int i=0;i<sources.length;i++)
		{
			if (i>0) urlSb.append(';');
			urlSb.append(cnt++);
		}
		urlSb.append("&destinations=");
		for (int i=0;i<destinations.length();i++)
		{
			if (i>0) urlSb.append(';');
			urlSb.append(cnt++);
		}
		
		long t_start = System.currentTimeMillis();
		//hostPort + "/table/v1/driving/16.369561009437817,48.20423271310815;16.37741831002266,48.20776186641345?sources=0&destinations=1"
		URL url = new URL(urlSb.toString());
		
		URLConnection con = url.openConnection();
			
		try (InputStream is= con.getInputStream()) {
			long t_callFinished = System.currentTimeMillis();
			
			OSRMClient.parseTableResult(results, new BufferedReader(new InputStreamReader(is)));
			long t_finished = System.currentTimeMillis();
			
			System.out.println(sources.length + "x" + destinations.length() 
					+ " t_call=" + (t_callFinished - t_start) 
					+ "ms t_parse="+ (t_finished - t_callFinished) + "ms " + url);
			
		}
	}

	private String baseUrl(ModeEnum mode) {
		return "http://" + host + ":" + (baseport + mode.osrmPortOffset);
	}
	
	public static double[][] parseTableResult(Reader jsonReader, int rows, int cols) throws JsonProcessingException, IOException
	{
		DoubleMatrixImpl dm = new DoubleMatrixImpl(rows, cols);
		parseTableResult(dm , jsonReader);
		return dm.arr();
	}
	
	public static void parseTableResult(DoubleMatrix durationArr, Reader jsonReader) throws JsonProcessingException, IOException
	{
		int rows = durationArr.rows(); int cols = durationArr.columns();
		
		JsonFactory jfactory = new JsonFactory();
		
		JsonParser jParser = jfactory.createParser(jsonReader);
		
		while (jParser.nextToken() != JsonToken.END_OBJECT) {
		    String fieldname = jParser.getCurrentName();
		
		    if ("code".equals(fieldname))
		    {
		    	jParser.nextToken();
		        String code = jParser.getText();
				if (!"Ok".equalsIgnoreCase(code))
				{
					throw new RuntimeException("osrm return code != ok");
				}
		    }
		    if ("durations".equals(fieldname))
		    {
		    	if (jParser.nextToken() != JsonToken.START_ARRAY) throw new RuntimeException("Expected START_ARRAY");
		    	int r = 0;
		    	while (jParser.nextToken() == JsonToken.START_ARRAY) {
		    	
		    		int c=0;
		    		while (jParser.nextToken() != JsonToken.END_ARRAY) {
		    			durationArr.set(r, c, jParser.getDoubleValue());
						c++;
		    		}
			    	if (c != cols) throw new RuntimeException("c != cols");
		    		r++;
		        }
		    	if (r != rows) throw new RuntimeException("r != rows");
		    }
		}
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
