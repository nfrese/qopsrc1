package at.qop.qoplib.imports;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import at.qop.qoplib.dbconnector.DbBatch;
import at.qop.qoplib.imports.UpdateAddresses;

//@Ignore
public class UpdateAddressesTest {
	
	int cnt = 0;
	@Test
	public void test() throws IOException {
		
		cnt = 0;
		UpdateAddresses updateAddresses = new UpdateAddresses("01");
		updateAddresses.onPacket(p -> {
			
			System.out.println(p);
			cnt+= p.records().size();
			return null;
		});
		updateAddresses.runUpdate();
		updateAddresses.done();
		
		System.out.println(cnt);
		System.out.println(updateAddresses.recordCount);
	}


}
