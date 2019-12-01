package at.qop.qoplib.integration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import org.junit.Assert;

public class QoplibIntegrationTest {

	  private static final Logger LOGGER = LoggerFactory.getLogger(QoplibIntegrationTest.class);
	  static Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(LOGGER);
	  
	  static String WAIT_PATTERN =
			    ".*database system is ready to accept connections.*\\s";
	  
	  @ClassRule
	  public static GenericContainer<?> postgres =
	    new GenericContainer<>("camptocamp/postgis:9.5")
	      .withClasspathResourceMapping("/integrationtests/qop_testdb.sql", "/docker-entrypoint-initdb.d/01_qop.sql", BindMode.READ_ONLY)
	      .withEnv("POSTGRES_USER", "qopuser")
	  	  .withEnv("POSTGRES_DB", "qop")
	  	  .withEnv("POSTGRES_PASSWORD", "autoxtest")
	  	  .withLogConsumer(logConsumer)
          .waitingFor(Wait.forLogMessage(WAIT_PATTERN, 2))
  		  ;

	  @Test
	  public void test() throws SQLException, InterruptedException {

		  try (Connection conn = connection()) {

			  ResultSet rs = conn.createStatement().executeQuery("select count(*) from public.autobahnanschluesse");
			  if (rs.next())
			  {
				  Assert.assertEquals(106, (long)rs.getObject(1));
			  }
		  }
	  }

	protected Connection connection() throws SQLException {
		String host = postgres.getContainerIpAddress();
		int port = postgres.getMappedPort(5432);
		String url = "jdbc:postgresql://"+host+":"+port+"/qop";
		String user = "qopuser";
		String password = "autoxtest";
		Connection conn = DriverManager.getConnection(url, user, password);
		return conn;
	}
	
}
