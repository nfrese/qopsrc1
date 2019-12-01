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

package at.qop.qoplib.extinterfaces.batch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import at.qop.qoplib.batch.BatchCalculationInMemory;
import at.qop.qoplib.batch.WriteBatTable.BatRecord;
import at.qop.qoplib.batch.WriteBatTable.ColGrp;
import at.qop.qoplib.batch.WriteSectionsHelper;
import at.qop.qoplib.calculation.CalculationSection;
import at.qop.qoplib.calculation.Rating;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Analysis;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileAnalysis;
import at.qop.qoplib.extinterfaces.json.QEXProfile;
import at.qop.qoplib.extinterfaces.json.QEXProfileAnalysis;

public abstract class BatchHandler {
	
	public String jsonCall(String jsonIn) throws JsonProcessingException, IOException
	{
		ObjectReader reader = new ObjectMapper().readerFor(QEXBatchInput.class);
		QEXBatchInput input = (QEXBatchInput)reader.readValue(jsonIn);
		
		Profile profile;
		if (input.profile != null)
		{
			profile = lookupProfile(input.profile);
			if (profile == null) throw new RuntimeException("profile " + input.profile + " not found!");
		} else {
			profile = readProfile(input.profileAdd);
		}
		
		List<Address> addresses = new ArrayList<>();
		
		for (QEXBatchSourceLocation source : input.sources)
		{
			Address address = new Address();
			address.geom = new GeometryFactory().createPoint(new Coordinate(source.lon, source.lat));
			address.gid = source.id;
			address.name = source.name;
			addresses.add(address);
		}
		
		BatchCalculationInMemory bc = createBC(profile, addresses);
		
		bc.run();
		
		List<BatRecord> output = bc.getOutput();
		
		QEXBatchOutput outBean = new QEXBatchOutput();
		outBean.profile = input.profile;
		
		WriteSectionsHelper sectionsHelper = new WriteSectionsHelper(profile);
		
		for (BatRecord record : output)
		{
			QEXBatchResult resultBean = new QEXBatchResult();
			resultBean.id = record.gid;
			resultBean.name = record.name;
			resultBean.lon = record.geom.getX();
			resultBean.lat = record.geom.getY();
			
			Rating<ColGrp> rating = sectionsHelper.rating(record);
			resultBean.overallRating = rating.overallRating;
			for (CalculationSection<ColGrp> rSection : rating.sections)
			{
				QEXBatchResultSection sectionBean = new QEXBatchResultSection();
				sectionBean.catid = rSection.generateSectionColumnid();
				sectionBean.rating = rSection.rating; 
				sectionBean.weight = rSection.weight;
				sectionBean.title = rSection.getTitle();
				
				resultBean.categories.add(sectionBean);
				
				for (ColGrp colGrp : rSection.lcs)
				{
					QEXBatchResultGrp grpBean = new QEXBatchResultGrp();
					grpBean.analysisid = colGrp.name;
					grpBean.result = colGrp.result;
					grpBean.unit = colGrp.getParams().analysis.analysisfunction.rvalUnit;
					grpBean.rating = colGrp.getRating();
					grpBean.weight = colGrp.getWeight();
					grpBean.description = colGrp.getParams().analysis.description;
					sectionBean.analyses.add(grpBean);
				}
			}
			
			outBean.results.add(resultBean);
		}
		
		PrettyPrinter pp = new DefaultPrettyPrinter();
		return new ObjectMapper().setDefaultPrettyPrinter(pp).enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(outBean);
	}

	private Profile readProfile(QEXProfile profileAdd) {
		Profile profile = new Profile();
		profile.name = profileAdd.name;
		profile.description = profileAdd.description;
		profile.aggrfn = profileAdd.aggrfn;
		for (QEXProfileAnalysis pab : profileAdd.profileAnalysis)
		{
			ProfileAnalysis pa = new ProfileAnalysis();
			
			pa.analysis = lookupAnalysis(pab.analysis_name);
			if (pa.analysis == null) throw new RuntimeException("analysis " + pab.analysis_name + " not found");
			
			pa.weight = pab.weight;
			pa.altratingfunc = pab.altratingfunc;
			pa.category = pab.category;
			pa.categorytitle = pab.categorytitle;
			pa.ratingvisible = pab.ratingvisible;			
			
		}
		
		return null;
	}

	protected abstract BatchCalculationInMemory createBC(Profile profile, List<Address> addresses);

	protected abstract Analysis lookupAnalysis(String analysis);
	
	protected abstract Profile lookupProfile(String profile);

}
