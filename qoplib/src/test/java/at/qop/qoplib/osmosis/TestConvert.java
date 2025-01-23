package at.qop.qoplib.osmosis;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;

import at.qop.qoplib.dbconnector.DBUtils;

public class TestConvert {

	@Test
	public void test() {
		Osmosis.main(new String[]{"--read-pbf", userDir() + "/Downloads/austria-latest.osm.pbf", "--node-key", "keyList=amenity", "--write-pgsimp-dump", "directory=/Users/norbert/Downloads/"});
	}

	@Test
	public void testWriteSql() throws FileNotFoundException {
		OsmosisPoisToDb.importAmenitys(userDir() + "/Downloads/austria-latest.osm.pbf", userDir() + "/Downloads/pois.sql", true);
	}

	private String userDir() {
		return System.getProperty("user.dir");
	}

	@Test
	public void testScript2Db() throws Exception {

		String jdbcUrl = System.getenv("IMPORT_DB_URL");
		String username = "postgres";
		String password = System.getenv("IMPORT_DB_PASSWORD");


		Class.forName("org.postgresql.Driver");

		Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

		DBUtils.importBatchScript(connection, userDir() + "/Downloads/pois.sql", 10000);
		connection.close();

	}

}
