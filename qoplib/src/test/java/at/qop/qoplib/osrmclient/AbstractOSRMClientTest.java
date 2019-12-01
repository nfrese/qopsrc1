package at.qop.qoplib.osrmclient;

import org.junit.ClassRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import at.qop.qoplib.Config.OSRMConf;

public class AbstractOSRMClientTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(OSRMClientTest.class);
	static Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(LOGGER);

	@ClassRule
	public static GenericContainer<?> osrmCar =
	new GenericContainer<>("qopimages/qop-routing-car:latest")
	.withExposedPorts(5300)
	.withLogConsumer(logConsumer)
	;

	@ClassRule
	public static GenericContainer<?> osrmFoot =
	new GenericContainer<>("qopimages/qop-routing-foot:latest")
	.withExposedPorts(5302)
	.withLogConsumer(logConsumer)
	;

	public static OSRMConf osrmConfig() {
		OSRMConf osrmConf = new OSRMConf();
		osrmConf.carHost = osrmCar.getContainerIpAddress();
		osrmConf.carPort = osrmCar.getMappedPort(5300);
		osrmConf.footHost = osrmFoot.getContainerIpAddress();
		osrmConf.footPort = osrmFoot.getMappedPort(5302);
		return osrmConf;
	}
}
