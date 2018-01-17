package at.qop.qoplib.batch;

import at.qop.qoplib.dbconnector.write.PerformUpdateAbstract;
import at.qop.qoplib.entities.Profile;

public class PerformBatUpdate extends PerformUpdateAbstract {

	final WriteBatTable wbt;
	
	public PerformBatUpdate(Profile profile)
	{
		wbt = new WriteBatTable(profile);
		wbt.onPacket(p -> forward(p));
		wbt.before();
	}
	
}
