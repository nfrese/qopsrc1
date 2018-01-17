package at.qop.qoplib.imports;

import at.qop.qoplib.dbconnector.write.PerformUpdateAbstract;

public class PerformAddressUpdate extends PerformUpdateAbstract {

	public void updateAddresses(String bezFilter)
	{
		bezFilter = bezFilter == null || bezFilter.trim().isEmpty() ? null : bezFilter; 
		
		try {
			UpdateAddresses updateAddresses = new UpdateAddresses(bezFilter);
			updateAddresses.onPacket(p -> forward(p));
			updateAddresses.runUpdate();
			updateAddresses.done();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
