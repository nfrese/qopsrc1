package at.qop.qoplib.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.ImagePullPolicy;
import org.testcontainers.utility.DockerImageName;

import at.qop.qoplib.osrmclient.OSRMClientTest;

public class QoplibIntegrationTestBase {
	
	protected static final String QOPDB_TEST_USER = "qopuser";
	protected static final String QOPDB_TEST_PASSWORD = "autoxtest";
	private static final Logger LOGGER = LoggerFactory.getLogger(QoplibIntegrationTest.class);
	static Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(LOGGER);

	static String WAIT_PATTERN =
			".*database system is ready to accept connections.*\\s";

	protected static GenericContainer<?> initPostgisContainer() {

		return new GenericContainer<>("camptocamp/postgis:9.6")
				.withClasspathResourceMapping("/integrationtests/qop_testdb.sql", "/docker-entrypoint-initdb.d/01_qop.sql", BindMode.READ_ONLY)
				.withEnv("POSTGRES_USER", QOPDB_TEST_USER)
				.withEnv("POSTGRES_DB", "qop")
				.withEnv("POSTGRES_PASSWORD", QOPDB_TEST_PASSWORD)
				.withLogConsumer(logConsumer).withReuse(false)
				.waitingFor(Wait.forLogMessage(WAIT_PATTERN, 2));
	}
	
	public static GenericContainer<?> initOsrmCarContainer() {
		return new GenericContainer<>("qopimages/qop-routing-car:latest")
				.withExposedPorts(5300)
				.withLogConsumer(logConsumer);
	}

	public static GenericContainer<?> initOsrmFootContainer() {
		return new GenericContainer<>("qopimages/qop-routing-foot:latest")
				.withExposedPorts(5302)
				.withLogConsumer(logConsumer);
	}

}
