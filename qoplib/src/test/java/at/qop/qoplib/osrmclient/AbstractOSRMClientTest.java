package at.qop.qoplib.osrmclient;

import org.junit.ClassRule;
import org.testcontainers.containers.GenericContainer;

import at.qop.qoplib.Config.OSRMConf;
import at.qop.qoplib.integration.QoplibIntegrationTestBase;

public class AbstractOSRMClientTest extends QoplibIntegrationTestBase {


	@ClassRule
	public static GenericContainer<?> osrmCar =	initOsrmCarContainer()
	;

	@ClassRule
	public static GenericContainer<?> osrmFoot = initOsrmFootContainer();
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
