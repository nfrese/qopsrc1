package at.qop.qoplib.domains;

import java.util.List;
import java.util.Set;

import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileAnalysis;
import at.qop.qoplib.entities.Analysis;
import at.qop.qoplib.entities.AnalysisFunction;

public interface IProfileDomain {
	
	List<Profile> listProfiles();

	void createProfile(Profile p);
	
	void updateProfile(Profile p);
	
	void dropProfile(Profile p);

	List<Analysis> listAnalyses();
	
	void createAnalysis(Analysis a);

	void updateAnalysis(Analysis a);
	
	void dropAnalysis(Analysis a);

	void createProfileAnalysis(Set<ProfileAnalysis> pas);

	void removeProfileAnalysis(Set<ProfileAnalysis> pas);

	void updateProfileAnalysis(ProfileAnalysis pa);

	List<AnalysisFunction> listAnalysisFunctions();

	void createAnalysisFunction(AnalysisFunction f);

	void updateAnalysisFunction(AnalysisFunction f);

	void dropAnalysisFunction(AnalysisFunction f);


}
