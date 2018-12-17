/* 
 * Copyright (C) 2018 Norbert Frese
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
*/

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
