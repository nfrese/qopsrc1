package at.qop.qoplib.batch;

import java.util.List;

import at.qop.qoplib.ConfigFile;
import at.qop.qoplib.Constants;
import at.qop.qoplib.calculation.DbLayerSource;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.osrmclient.OSRMClient;

public class BatchCalculationInMemoryImpl extends BatchCalculationInMemory {

	public BatchCalculationInMemoryImpl(Profile currentProfile, List<Address> addresses) {
		super(currentProfile, addresses);
	}

	@Override
	protected OSRMClient initRouter() {
		ConfigFile cf = ConfigFile.read();
		return new OSRMClient(cf.getOSRMHost(), cf.getOSRMPort(), Constants.SPLIT_DESTINATIONS_AT);
	}
	
	@Override
	protected DbLayerSource initSource() {
		return new DbLayerSource();
	}

}
