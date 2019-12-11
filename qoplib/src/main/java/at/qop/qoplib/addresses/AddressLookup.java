package at.qop.qoplib.addresses;

import java.util.List;

import at.qop.qoplib.entities.Address;

public interface AddressLookup {
	List<Address> fetchAddresses(
			int offset,
			int limit,
			String namePrefix);
	int getAddressCount(String namePrefix);
}