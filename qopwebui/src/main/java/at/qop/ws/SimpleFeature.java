package at.qop.ws;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import at.qop.ws.QOPRestApiRoute.RoutingResults;

public class SimpleFeature {
	public String id;
	public Map<String,Object> properties = new LinkedHashMap<>();
	public JsonNode geometry;
}