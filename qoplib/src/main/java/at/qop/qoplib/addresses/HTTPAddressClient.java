package at.qop.qoplib.addresses;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import at.qop.qoplib.calculation.CRSTransform;
import at.qop.qoplib.entities.Address;

public class HTTPAddressClient implements AddressLookup {
	
	private final String urlBase;
	
	public HTTPAddressClient(String urlBase) {
		super();
		this.urlBase = urlBase;
	}
	
	private GeometryFactory gf() { return CRSTransform.gfWGS84; }
	
	public List<Address> fetchAddresses(
			int offset,
			int limit,
			String queryTxt) {
		List<Address> results = new ArrayList<Address>();
		if (queryTxt == null) return results;
		
		URL url;
		try {
			url = new URL(urlBase + URLEncoder.encode(queryTxt, "UTF-8"));
			URLConnection con = url.openConnection();
			try (InputStream is= con.getInputStream()) {
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				
				ObjectMapper mapper = new ObjectMapper();
				JsonNode root = mapper.readTree(reader);
				
	            if (root.isArray()) {
	                for (JsonNode node : root) {
	                	
	                	String lat = node.path("lat").asText();
	                	String lon = node.path("lon").asText();
	                	String display_name = node.path("display_name").asText();
	                	
	                	Address address = new Address();
	                	address.geom = gf().createPoint(new Coordinate(Double.valueOf(lon), Double.valueOf(lat)));
	                	address.name = display_name;
	                	results.add(address);
	                }
	            }
			}
			
			return results.subList(offset, Math.min(results.size(), offset+limit));
			
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public int getAddressCount(String namePrefix) {
		return fetchAddresses(0, 100000, namePrefix).size();
	}

}
