package at.qop.qoplib.extinterfaces.batch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import at.qop.qoplib.batch.BatchCalculationInMemory;
import at.qop.qoplib.batch.WriteBatTable.BatRecord;
import at.qop.qoplib.batch.WriteBatTable.ColGrp;
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
		
		for (BatRecord record : output)
		{
			QEXBatchResult resultBean = new QEXBatchResult();
			resultBean.id = record.gid;
			resultBean.name = record.name;
			resultBean.lon = record.geom.getX();
			resultBean.lat = record.geom.getY();
			
			for (ColGrp colGrp : record.colGrps)
			{
				QEXBatchResultGrp grpBean = new QEXBatchResultGrp();
				grpBean.name = colGrp.name;
				grpBean.result = colGrp.result;
				grpBean.rating = colGrp.getRating();
				grpBean.weight = colGrp.getWeight();
				
				resultBean.grps.add(grpBean);
			}
			
			outBean.results.add(resultBean);
		}
		
		return new ObjectMapper().writeValueAsString(outBean);
	}

	protected abstract BatchCalculationInMemory createBC(Profile profile, List<Address> addresses);

	protected abstract Profile lookupProfile(String profile);

}
