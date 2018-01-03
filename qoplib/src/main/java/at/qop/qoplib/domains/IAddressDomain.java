package at.qop.qoplib.domains;

import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

import at.qop.qoplib.entities.Address;

public interface IAddressDomain {
	
	List<Address> findAddresses(Geometry filter);

	List<Address> findAddresses(String searchTxt);

	List<Address> findAddresses(int offset, int limit, String namePrefix);

	int countAddresses(String namePrefix);

}
