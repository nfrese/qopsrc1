package at.qop.qoplib.extinterfaces.batch;

import java.util.List;

import at.qop.qoplib.batch.BatchCalculationInMemoryImpl;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Profile;

public class BatchHandlerImpl extends BatchHandler {

	@Override
	protected Profile lookupProfile(String profile) {
		// TODO Auto-generated method stub
		return null;
	}

	protected BatchCalculationInMemoryImpl createBC(Profile profile, List<Address> addresses) {
		return new BatchCalculationInMemoryImpl(profile, addresses);
	}
	
}
