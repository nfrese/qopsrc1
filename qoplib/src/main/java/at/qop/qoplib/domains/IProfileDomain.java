package at.qop.qoplib.domains;

import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileLayer;

public interface IProfileDomain {
	
	List<Profile> listProfiles();

	void dropProfile(Profile profile);

	void createProfile(Profile profile);

	void createProfileLayer(ProfileLayer profile);

}
