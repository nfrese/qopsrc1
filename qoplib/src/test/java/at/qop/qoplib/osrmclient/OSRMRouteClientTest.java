package at.qop.qoplib.osrmclient;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import at.qop.qoplib.entities.ModeEnum;

public class OSRMRouteClientTest {

	private static final String OSRM_SERVER = "http://10.0.0.17:5000";

	@Test
	public void parseResultTest() throws JsonProcessingException, IOException {

		String json = "{\"code\":\"Ok\",\"routes\":[{\"geometry\":{\"coordinates\":[[16.375171,48.208803],[16.376252,48.208403],[16.377449,48.208029],[16.380121,48.20724],[16.380477,48.207161],[16.379745,48.206449],[16.378604,48.206973]],\"type\":\"LineString\"},\"legs\":[{\"steps\":[],\"distance\":632.9,\"duration\":96.9,\"summary\":\"\",\"weight\":96.9}],\"distance\":632.9,\"duration\":96.9,\"weight_name\":\"routability\",\"weight\":96.9}],\"waypoints\":[{\"hint\":\"J10BgP___38TAAAAFwAAAD8AAAAAAAAAEwAAABcAAAA_AAAAAAAAAMYAAACD3fkAo5vfAsjd-QDim98CAQDfB5akHto=\",\"name\":\"Wollzeile\",\"location\":[16.375171,48.208803]},{\"hint\":\"smEAgP___38iAAAAMwAAAAAAAAC1AAAAIgAAADMAAAAAAAAAtQAAAMYAAADs6vkAfZTfAo7r-QAZld8CAAAPE5akHto=\",\"name\":\"Zedlitzgasse\",\"location\":[16.378604,48.206973]}]}";
		LonLat[] vertices = OSRMClient.parseRouteResult(json);
		
		Assert.assertEquals(7, vertices.length);
		Assert.assertEquals(new LonLat(16.378604, 48.206973), vertices[6]);
	}
		
}
