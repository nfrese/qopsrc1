package at.qop.qoplib;

import java.io.IOException;

import org.junit.Test;

import at.qop.qoplib.dbconnector.DbBatch;

public class UpdateAddressesTest {
	
	@Test
	public void test() throws IOException {
		
		UpdateAddresses updateAddresses = new UpdateAddresses();
		updateAddresses.onPacket(p -> forward(p));
		updateAddresses.runUpdate();
	}

	private Void forward(DbBatch p) {
		System.out.println(p);
		return null;
	}

}
