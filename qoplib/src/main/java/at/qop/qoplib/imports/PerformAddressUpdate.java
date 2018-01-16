package at.qop.qoplib.imports;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.dbconnector.DbBatch;
import at.qop.qoplib.domains.IGenericDomain;

public class PerformAddressUpdate {

	public void updateAddresses(String bezFilter)
	{
		bezFilter = bezFilter == null || bezFilter.trim().isEmpty() ? null : bezFilter; 
		
		try {
			UpdateAddresses updateAddresses = new UpdateAddresses(bezFilter);
			updateAddresses.onPacket(p -> forward(p));
			updateAddresses.runUpdate();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private Void forward(DbBatch p) {
		System.out.println(p);
		
		try {
			IGenericDomain gd = LookupSessionBeans.genericDomain();
			gd.batchUpdate(p);
		} catch (Exception ex)
		{
			if (p.mayFail)
			{
				System.err.println("MAYFAIL: " + ex.getMessage());
			}
			else
			{
				throw new RuntimeException(ex);
			}
		}
		
		return null;
	}
	
}
