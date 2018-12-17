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

package at.qop.qoplib.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import at.qop.qoplib.batch.WriteBatTable.BatRecord;
import at.qop.qoplib.batch.WriteBatTable.ColGrp;
import at.qop.qoplib.calculation.CalculationSection;
import at.qop.qoplib.calculation.ISectionBuilderInput;
import at.qop.qoplib.calculation.Rating;
import at.qop.qoplib.calculation.SectionBuilder;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileAnalysis;

public class WriteSectionsHelper {

	private final Profile currentProfile;
	private final ArrayList<CalculationSection<ISectionBuilderInput>> sections;
	
	public WriteSectionsHelper(Profile currentProfile) {
		super();
		this.currentProfile = currentProfile;
		
		List<ISectionBuilderInput> ll = this.currentProfile.profileAnalysis.stream().map(
				pa -> { return new ISectionBuilderInput() {

					@Override
					public ProfileAnalysis getParams() {
						return pa;
					}

				}; }
				).collect(Collectors.toList());

		sections = new SectionBuilder<ISectionBuilderInput>(ll).run();	
	}

	public ArrayList<CalculationSection<ISectionBuilderInput>> getSections() {
		return sections;
	}
	
	public int numSections() {
		return sections.size();
	}
	
	private List<CalculationSection<ColGrp>> assignedSections(BatRecord record) {
		List<CalculationSection<ColGrp>> _sects = new ArrayList<>();
		
		for (CalculationSection<ISectionBuilderInput> s : sections)
		{
			CalculationSection<ColGrp> cgrS = new CalculationSection<ColGrp>();
			
			for (ISectionBuilderInput lc : s.lcs)
			{
				for (ColGrp grp : record.colGrps)
				{
					if (lc.getParams().analysis.name.equals(grp.pa.analysis.name))
					{
						cgrS.lcs.add(grp);
					}
				}					
			}
			_sects.add(cgrS);
			
		}
		return _sects;
	}
	
	public Rating<ColGrp> rating(BatRecord record) {

		List<CalculationSection<ColGrp>> _sects = assignedSections(record);

		Rating<ColGrp> rating = new Rating<ColGrp>(currentProfile, _sects);
		rating.runRating();
		return rating;
	}

}
