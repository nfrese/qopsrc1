package at.qop.qoplib.osrmclient;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class OSRMClientTest {

	@Test
	public void test() throws IOException {
		
		OSRMClient client = new OSRMClient("http://10.0.0.17:5000");
		
		LatLon[] sources = new LatLon[] {new LatLon(16.369561009437817,48.20423271310815)};
		LatLon[] targets = new LatLon[] {new LatLon(16.37741831002266,48.20776186641345)};
		String result = client.table(sources, targets);
		System.out.println(result);
		
	}

}
