package at.qop.qoplib.dbconnector;

import org.junit.Assert;
import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;

public class DBUtilsTest {

	@Test
	public void test() {
		String obj = "BOX(16.1872075686366 48.1212174268439,16.5521039991526 48.3186709198307)";
		Envelope result = DBUtils.parsePGEnvelope(obj);
		Assert.assertEquals("Env[16.1872075686366 : 16.5521039991526, 48.1212174268439 : 48.3186709198307]", result +"");
	}

}
