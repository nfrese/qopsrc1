package at.qop.qoplib.integration;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.qop.qoplib.Constants;
import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.batch.BatchCalculationInMemory;
import at.qop.qoplib.batch.BatchCalculationInMemoryImpl;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Analysis;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.extinterfaces.batch.BatchHandler;
import at.qop.qoplib.extinterfaces.batch.QEXBatchInput;
import at.qop.qoplib.extinterfaces.batch.QEXBatchSourceLocation;
import at.qop.qoplib.extinterfaces.json.QEXProfile;
import at.qop.qoplib.extinterfaces.json.QEXProfileAnalysis;
import at.qop.qoplib.osrmclient.OSRMClient;

public class QopLibJPAIntegrationTest extends QoplibIntegrationTest {

	@Test
	public void testSupermarketsRPlatz() throws JsonProcessingException, IOException
	{
		initTestContext();
		
	
		QEXProfile profileBean = new QEXProfile();

		profileBean.name = "custom1";
		profileBean.description = "...";
		profileBean.aggrfn = null;
		
		{
			QEXProfileAnalysis pab = new QEXProfileAnalysis();
			pab.analysis_name = "Supermarkt";
			pab.weight= 1.0;
			pab.altratingfunc = null;
			pab.category = "1.1";
			pab.categorytitle = "Cat1";
			pab.ratingvisible = true;
			profileBean.profileAnalysis.add(pab);
		}		
		
		QEXBatchInput input = new QEXBatchInput();
		input.profile = null;
		input.customProfile = profileBean;
		addSources(input);
		
		String json = new ObjectMapper().writeValueAsString(input);
		System.out.println(json);
		
		BatchHandler bh = new MyBatchHandler();
		
		String jsonOut = bh.jsonCall(json);
		
		System.out.println(jsonOut);
		Assert.assertTrue(jsonOut.contains("\"result\" : 4.0,"));
		Assert.assertTrue(jsonOut.contains("\"result\" : 3.0,"));
	
	}

	protected void addSources(QEXBatchInput input) {
		{
			QEXBatchSourceLocation source = new QEXBatchSourceLocation();
			source.id = 1;
			source.name = "Lat/Lon R.platz 1";
			
			source.lon = 16.38970161567575; 
			source.lat = 48.21044134351946;
			
			input.sources.add(source);
		}
		
		{
			QEXBatchSourceLocation source = new QEXBatchSourceLocation();
			source.id = 2;
			source.name = "Q.Straße 160";
			
			source.lon = 16.357969708589458; 
			source.lat = 48.177949361447176;
			
			input.sources.add(source);
		}
	}
	
	@Test
	public void testRaster1RPlatz() throws JsonProcessingException, IOException
	{
		initTestContext();
	
		QEXProfile profileBean = new QEXProfile();

		profileBean.name = "custom2";
		profileBean.description = "...";
		profileBean.aggrfn = null;
		
		{
			QEXProfileAnalysis pab = new QEXProfileAnalysis();
			pab.analysis_name = "raster1";
			pab.weight= 1.0;
			pab.altratingfunc = null;
			pab.category = "1.1";
			pab.categorytitle = "Cat1";
			pab.ratingvisible = true;
			profileBean.profileAnalysis.add(pab);
		}		
		
		QEXBatchInput input = new QEXBatchInput();
		input.profile = null;
		input.customProfile = profileBean;
		addSources(input);
		
		String json = new ObjectMapper().writeValueAsString(input);
		System.out.println(json);
		
		BatchHandler bh = new MyBatchHandler();
		
		String jsonOut = bh.jsonCall(json);
		
		System.out.println(jsonOut);
		Assert.assertTrue(jsonOut.contains("\"result\" : 74.52"));
		Assert.assertTrue(jsonOut.contains("\"result\" : 73.475"));
	}

	protected void initTestContext() {
		LookupSessionBeans.inTestContext = true;
		LookupSessionBeans.jdbcTestConnectionUrl = connectionUrl();
		LookupSessionBeans.jdbcTestConnectionUserName = QOPDB_TEST_USER;
		LookupSessionBeans.jdbcTestConnectionPassword = QOPDB_TEST_PASSWORD;
	}	
	
	private final class MyBatchHandler extends BatchHandler {
		@Override
		protected BatchCalculationInMemory createBC(Profile profile, List<Address> addresses) {
			return new BatchCalculationInMemoryImpl(profile, addresses) {
				@Override
				protected OSRMClient initRouter() {
					return new OSRMClient(osrmConfig(), Constants.SPLIT_DESTINATIONS_AT);
				}
			};
		}

		@Override
		protected Profile lookupProfile(String profileName) {
			
			
			List<Profile> profiles = LookupSessionBeans.profileDomain().listProfiles();
			for (Profile profile : profiles)
			{
				if (profile.name.equals(profileName))
				{
					//cfg.checkUserProfile(username, profile.name);
					return profile;
				}
			}
			return null;
		}

		@Override
		protected Analysis lookupAnalysis(String analysisName) {
			List<Analysis> profiles = LookupSessionBeans.profileDomain().listAnalyses();
			for (Analysis analysis : profiles)
			{
				if (analysis.name.equals(analysisName))
				{
					return analysis;
				}
			}
			return null;
		}
	}


	
}
