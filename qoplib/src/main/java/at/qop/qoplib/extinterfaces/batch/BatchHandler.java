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
import at.qop.qoplib.entities.Profile;

public abstract class BatchHandler {
	
	public String jsonCall(String jsonIn) throws JsonProcessingException, IOException
	{
		ObjectReader reader = new ObjectMapper().readerFor(QEXBatchInput.class);
		QEXBatchInput input = (QEXBatchInput)reader.readValue(jsonIn);
		
		
		Profile profile = lookupProfile(input.profile);
		if (profile == null) throw new RuntimeException("profile " + input.profile + " not found!");
		
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
				sectionBean.catid = rSection.getSectionColumnName();
				sectionBean.rating = rSection.rating; 
				sectionBean.weight = rSection.weight;
				sectionBean.title = rSection.getTitle();
				
				resultBean.categories.add(sectionBean);
				
				for (ColGrp colGrp : rSection.lcs)
				{
					QEXBatchResultGrp grpBean = new QEXBatchResultGrp();
					grpBean.name = colGrp.name;
					grpBean.result = colGrp.result;
					grpBean.rating = colGrp.getRating();
					grpBean.weight = colGrp.getWeight();
					sectionBean.analyses.add(grpBean);
				}
			}
			
			outBean.results.add(resultBean);
		}
		
		PrettyPrinter pp = new DefaultPrettyPrinter();
		return new ObjectMapper().setDefaultPrettyPrinter(pp).enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(outBean);
	}

	protected abstract BatchCalculationInMemory createBC(Profile profile, List<Address> addresses);

	protected abstract Profile lookupProfile(String profile);

}
