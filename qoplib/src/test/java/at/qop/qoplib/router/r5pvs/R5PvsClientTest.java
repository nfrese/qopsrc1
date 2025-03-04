package at.qop.qoplib.router.r5pvs;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import at.qop.qoplib.Utils;
import at.qop.qoplib.osrmclient.LonLat;
import at.qop.qoplib.router.r5pvs.R5PvsClient.TableResult;

public class R5PvsClientTest {

	@Test
	public void test() throws IOException {
		R5PvsClient client = new R5PvsClient();
		
		LonLat[] sources = new LonLat[] {
				new LonLat(16.6000785,48.4526522)
		};
		
		LonLat[] destinations = new LonLat[] {
				new LonLat(16.6043233,48.4504615), 
				new LonLat(16.6008523,48.4515231), 
				new LonLat(16.5992913,48.453420900000005), 
				new LonLat(16.5891682,48.4721842), 
				new LonLat(16.6012256,48.453260900000004),
				new LonLat(16.5915001, 48.4781261)
		};
		
		R5PvsClient.TableResult tr = new TableResult();
		client.table_(tr, null, sources , destinations);
		
		System.out.println(tr);
	}
	
	
//	/qoplib/src/main/resources/at/qop/qoplib/router/r5pvs/response.json
	
	
	@Test
	public void testParse() throws JsonProcessingException, IOException {
		String json = Utils.readResourceToString("/at/qop/qoplib/router/r5pvs/response.json");
		
		R5PvsClient.TableResult tr = new TableResult();
		R5PvsClient.parseTableResult(tr, new StringReader(json));
		
		System.out.println(tr);
		
		
		
	}
	
}
