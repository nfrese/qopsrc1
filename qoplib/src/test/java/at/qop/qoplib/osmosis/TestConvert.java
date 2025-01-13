package at.qop.qoplib.osmosis;

import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;

public class TestConvert {
	
	@Test
	public void test() {
		Osmosis.main(new String[]{"--read-pbf", "/Users/norbert/Downloads/wien.pbf", "--node-key", "keyList=\"amenity\"", "--wx", "OUTPUT.osm"});
	}

}
