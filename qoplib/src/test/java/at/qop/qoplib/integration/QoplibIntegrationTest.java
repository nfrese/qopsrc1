package at.qop.qoplib.integration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import at.qop.qoplib.osrmclient.AbstractOSRMClientTest;

public class QoplibIntegrationTest extends AbstractOSRMClientTest {

	@ClassRule
	public static GenericContainer<?> postgres = initPostgisContainer();


	@Test
	public void test1() throws SQLException, InterruptedException {

		try (Connection conn = connection()) {

			ResultSet rs = conn.createStatement().executeQuery("select count(*) from public.autobahnanschluesse");
			if (rs.next())
			{
				Assert.assertEquals(106, (long)rs.getObject(1));
			}
		}
	}

	@Test
	public void test2() throws SQLException, InterruptedException {

		try (Connection conn = connection()) {

			ResultSet rs = conn.createStatement().executeQuery("select count(*) from public.\"1\"");
			if (rs.next())
			{
				Assert.assertEquals(1, (long)rs.getObject(1));
			}
		}
	}
	
	@Test
	public void test3a() throws SQLException, InterruptedException {
		System.out.println("containerid=" + postgres.getContainerId());
		
		try (Connection conn = connection()) {

			ResultSet rs = conn.createStatement().executeQuery("select * from public.q_analysis");
			while (rs.next())
			{
				for (int i=0;i<rs.getMetaData().getColumnCount();i++)
				{
					System.out.print(rs.getObject(i+1) + "\t");
				}
				System.out.println();
			}
		}
	}
	
	@Test
	public void test3() throws SQLException, InterruptedException {

		try (Connection conn = connection()) {

			ResultSet rs = conn.createStatement().executeQuery("select count(*) from public.q_analysis");
			if (rs.next())
			{
				Assert.assertEquals(5, (long)rs.getObject(1));
			}
		}
	}
	
	protected Connection connection() throws SQLException {
		String url = connectionUrl();
		String user = QOPDB_TEST_USER;
		String password = QOPDB_TEST_PASSWORD;
		Connection conn = DriverManager.getConnection(url, user, password);
		return conn;
	}

	protected String connectionUrl() {
		String host = postgres.getContainerIpAddress();
		int port = postgres.getMappedPort(5432);
		String url = "jdbc:postgresql://"+host+":"+port+"/qop";
		return url;
	}

}
