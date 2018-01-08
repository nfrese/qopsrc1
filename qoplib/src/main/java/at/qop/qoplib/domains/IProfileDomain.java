package at.qop.qoplib.domains;

import java.util.List;
import java.util.Set;

import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileAnalysis;
import at.qop.qoplib.entities.Analysis;

public interface IProfileDomain {
	
	List<Profile> listProfiles();

	void createProfile(Profile profile);
	
	void updateProfile(Profile profile);
	
	void dropProfile(Profile profile);

	List<Analysis> listAnalyses();
	
	void createAnalysis(Analysis analysis);

	void updateAnalysis(Analysis profile);
	
	void dropAnalysis(Analysis profile);

	void createProfileAnalysis(Set<ProfileAnalysis> addedSelection);

	void removeProfileAnalysis(Set<ProfileAnalysis> removedSelection);


}
