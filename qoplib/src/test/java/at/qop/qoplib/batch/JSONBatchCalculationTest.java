package at.qop.qoplib.batch;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.extinterfaces.batch.BatchHandler;
import at.qop.qoplib.extinterfaces.batch.QEXBatchInput;
import at.qop.qoplib.extinterfaces.batch.QEXBatchSourceLocation;

public class JSONBatchCalculationTest extends BatchCalculationTest {
	
	@Test
	public void jsonOutTest() throws IOException
	{
		QEXBatchInput input = new QEXBatchInput();
		input.profile = "Wohnen";
		{
			QEXBatchSourceLocation source = new QEXBatchSourceLocation();
			source.id = 1;
			source.name = "Location1";
			source.lon = 16.3724265546418; 
			source.lat = 48.2061121370655;
			
			input.sources.add(source);
		}
		
		{
			QEXBatchSourceLocation source = new QEXBatchSourceLocation();
			source.id = 2;
			source.name = "Location2";
			source.lon = 16.3695610097329;  
			source.lat = 48.2042327131692;
			
			input.sources.add(source);
		}

		
		String json = new ObjectMapper().writeValueAsString(input);
		System.out.println(json);
		
		ObjectReader reader = new ObjectMapper().readerFor(QEXBatchInput.class);
		QEXBatchInput rinput = (QEXBatchInput)reader.readValue(json);
		 
		Assert.assertEquals(16.3724265546418, rinput.sources.stream().filter(s -> s.name.equals("Location1")).findFirst().get().lon, 0.0001);
	}
	
	@Test
	public void testAll() throws JsonProcessingException, IOException
	{
		BatchHandler bh = new BatchHandler() {

			@Override
			protected BatchCalculationInMemory createBC(Profile profile, List<Address> addresses) {
				return initBC(addresses, profile);
			}

			@Override
			protected Profile lookupProfile(String profile) {
				return createProfile();
			}};

			
		String jsonIn = "{\"profile\":\"Wohnen\",\"sources\":[{\"id\":1,\"name\":\"Location1\",\"lat\":48.2061121370655,\"lon\":16.3724265546418},{\"id\":2,\"name\":\"Location2\",\"lat\":48.2042327131692,\"lon\":16.3695610097329}]}";
		String jsonResult = bh.jsonCall(jsonIn);
		
		System.out.println(jsonResult);
		
	}
	

}
