package at.qop.qoplib.osmosis;

import java.io.FileNotFoundException;

import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;

public class TestConvert {
	
	@Test
	public void test() {
		Osmosis.main(new String[]{"--read-pbf", "/Users/norbert/Downloads/austria-latest.osm.pbf", "--node-key", "keyList=amenity", "--write-pgsimp-dump", "directory=/Users/norbert/Downloads/"});
	}
	
	@Test
	public void testc() throws FileNotFoundException {
		Mysink.importAmenitys("/Users/norbert/Downloads/austria-latest.osm.pbf", "/Users/norbert/Downloads/pois.sql");
	}

}
