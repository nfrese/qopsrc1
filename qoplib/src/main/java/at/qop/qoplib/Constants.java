package at.qop.qoplib;

import java.util.Arrays;
import java.util.List;

public class Constants {
	
	public static final List<String> CONFIG_TABLES 
		= Arrays.asList("q_analysis", "q_analysisfunction", "q_config", "q_profile", "q_profileanalysis");

	public static final int SPLIT_DESTINATIONS_AT = 3000;

	public static final String BATCH_CALCULATION_SAMPLE_JSON = "{\"profile\":\"Wohnen\",\"sources\":[{\"id\":1,\"name\":\"Location1\",\"lat\":48.2061121370655,\"lon\":16.3724265546418},{\"id\":2,\"name\":\"Location2\",\"lat\":48.2042327131692,\"lon\":16.3695610097329}]}";
	
}
