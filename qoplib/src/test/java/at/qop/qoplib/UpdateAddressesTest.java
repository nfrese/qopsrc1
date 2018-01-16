package at.qop.qoplib;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import at.qop.qoplib.dbconnector.DbBatch;
import at.qop.qoplib.imports.UpdateAddresses;

@Ignore
public class UpdateAddressesTest {
	
	@Test
	public void test() throws IOException {
		
		UpdateAddresses updateAddresses = new UpdateAddresses("01");
		updateAddresses.onPacket(p -> forward(p));
		updateAddresses.runUpdate();
	}

	private Void forward(DbBatch p) {
		System.out.println(p);
		return null;
	}

}
