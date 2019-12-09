package at.qop.qoplib.integration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;

import at.qop.qoplib.Constants;
import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.Utils;
import at.qop.qoplib.batch.BatchCalculationInMemory;
import at.qop.qoplib.batch.BatchCalculationInMemoryImpl;
import at.qop.qoplib.batch.WriteBatTable.BatRecord;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Analysis;
import at.qop.qoplib.entities.AnalysisFunction;
import at.qop.qoplib.entities.ModeEnum;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileAnalysis;
import at.qop.qoplib.extinterfaces.batch.BatchHandler;
import at.qop.qoplib.extinterfaces.batch.QEXBatchInput;
import at.qop.qoplib.extinterfaces.batch.QEXBatchSourceLocation;
import at.qop.qoplib.extinterfaces.json.ExportProfile;
import at.qop.qoplib.extinterfaces.json.QEXProfile;
import at.qop.qoplib.extinterfaces.json.QEXProfileAnalysis;
import at.qop.qoplib.osrmclient.OSRMClient;

public class QopLibJPAIntegrationTest extends QoplibIntegrationTest {

	@Test
	public void test() throws JsonProcessingException, IOException
	{
		LookupSessionBeans.inTestContext = true;
		LookupSessionBeans.jdbcTestConnectionUrl = connectionUrl();
		LookupSessionBeans.jdbcTestConnectionUserName = QOPDB_TEST_USER;
		LookupSessionBeans.jdbcTestConnectionPassword = QOPDB_TEST_PASSWORD;
		
	
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
		{
			QEXBatchSourceLocation source = new QEXBatchSourceLocation();
			source.id = 1;
			source.name = "Lat/Lon Radetzkyplatz 1";
			
			source.lon = 16.38970161567575; 
			source.lat = 48.21044134351946;
			
			input.sources.add(source);
		}
		
		{
			QEXBatchSourceLocation source = new QEXBatchSourceLocation();
			source.id = 2;
			source.name = "Location2";
			source.lon = 16.3695610097329;  
			source.lat = 48.2042327131692;
			
			//input.sources.add(source);
		}

		
		String json = new ObjectMapper().writeValueAsString(input);
		System.out.println(json);
		
		
		BatchHandler bh = new BatchHandler() {

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
			
		};
		
		String jsonOut = bh.jsonCall(json);
		
		
		System.out.println(jsonOut);
//		
//		BatRecord r1 = output.stream().filter(r -> r.name.equals("Rauhensteingasse 10")).findFirst().get();
//		Assert.assertEquals(2.96, r1.colGrps[0].result, 0.1);

		
	}
	

	
}
