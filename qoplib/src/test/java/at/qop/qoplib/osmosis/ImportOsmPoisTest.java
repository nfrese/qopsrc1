package at.qop.qoplib.osmosis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;

import at.qop.qoplib.dbconnector.DBUtils;

public class ImportOsmPoisTest {

	@Test
	public void test() {
		Osmosis.main(new String[]{"--read-pbf", localPbfFile(), "--node-key", "keyList=amenity", "--write-pgsimp-dump", "directory=/Users/norbert/Downloads/"});
	}

	@Test
	public void testExtractBoundaries() {
		
		// wget -O weinviertel.poly "https://polygons.openstreetmap.fr/get_poly.py?id=11317717&params=0"
		
		Osmosis.main(new String[]{
				"--read-pbf", 
				userDir() + "/work/qop/pbf/austria-latest.osm.pbf", 
				"--bounding-polygon", 
				"completeWays=yes", "file=" + userDir() + "/work/qop/pbf/weinviertel.poly",
				"--write-pbf", 
				userDir() + "/work/qop/pbf/weinviertel-latest.osm.pbf"});
	}
	
	@Test
	public void testWriteSql() throws FileNotFoundException {
		OsmosisPoisToDb.importAmenitys(localPbfFile(), sqlScriptFilename(), true);
	}

	private String localPbfFile() {
		return userDir() + "/Downloads/austria-latest.osm.pbf";
	}

	private String userDir() {
		return "/Users/norbert"; //System.getProperty("user.dir");
	}

	@Test
	public void testScript2Db() throws Exception {

		String sqlScriptFilename = sqlScriptFilename();
		
		OsmosisPoisToDb.importScript2DB(sqlScriptFilename);

	}

	private String sqlScriptFilename() {
		return userDir() + "/Downloads/pois.sql";
	}

	@Test
	public void importAll() throws MalformedURLException, FileNotFoundException, IOException {
		
		String pbfUrl = "https://download.geofabrik.de/europe/austria-latest.osm.pbf";
	 	
	 	String qopWorkingDir = userDir() + "/work/qop/pbf/";
		String localPbfPath = qopWorkingDir + "austria-latest.osm.pbf";
		String localREducedPolyPath = userDir() + "/work/qop/pbf/weinviertel.poly";
		String localREducedPbfPath = qopWorkingDir + "weinviertel-latest.osm.pbf";
		
		 
		OsmosisPoisToDb.importAll(pbfUrl, qopWorkingDir, localPbfPath, localREducedPolyPath, localREducedPbfPath);
	}
	
	
}
