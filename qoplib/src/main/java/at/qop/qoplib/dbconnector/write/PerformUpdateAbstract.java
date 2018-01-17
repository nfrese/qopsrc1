package at.qop.qoplib.dbconnector.write;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.dbconnector.DbBatch;
import at.qop.qoplib.domains.IGenericDomain;

public class PerformUpdateAbstract {

	protected Void forward(DbBatch p) {
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
