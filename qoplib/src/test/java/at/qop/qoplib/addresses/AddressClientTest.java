package at.qop.qoplib.addresses;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import at.qop.qoplib.Config;
import at.qop.qoplib.entities.Address;

public class AddressClientTest {
	
	@Test
	public void test() {
		
		HTTPAddressClient ac = new HTTPAddressClient(Config.read().getAddressLookupURL());
		List<Address> results = ac.fetchAddresses(0, 1000, "Radetzkyplatz 1");
		
		for (Address result : results)
		{
			System.out.println(result);
		}
		
		Assert.assertTrue(String.valueOf(results).contains("Gasthaus Wild"));
		Assert.assertTrue(results.size() == 2);
	}

}
