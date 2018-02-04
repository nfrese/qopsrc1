package at.qop.qoplib.batch;

import java.io.IOException;

import at.qop.qoplib.dbconnector.write.PerformUpdateAbstract;

public class PerformDelete extends PerformUpdateAbstract {

	final DropTable drt;
	
	public PerformDelete(String tname)
	{
		drt = new DropTable(tname);
		drt.onPacket(p -> forward(p));
		try {
			drt.runUpdate();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
