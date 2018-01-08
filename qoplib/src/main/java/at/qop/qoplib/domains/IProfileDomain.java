package at.qop.qoplib.domains;

import java.util.List;

import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileLayer;

public interface IProfileDomain {
	
	List<Profile> listProfiles();

	void createProfile(Profile profile);
	
	void updateProfile(Profile profile);
	
	void dropProfile(Profile profile);

	void createProfileLayer(ProfileLayer profile);


}
