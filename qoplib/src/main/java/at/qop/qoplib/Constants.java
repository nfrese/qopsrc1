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

package at.qop.qoplib;

import java.util.Arrays;
import java.util.List;

public class Constants {
	
	public static final List<String> CONFIG_TABLES 
		= Arrays.asList("q_analysis", "q_analysisfunction", "q_config", "q_profile", "q_profileanalysis");

	public static final String Q_ADDRESSES = "q_addresses";
	
	public static final int SPLIT_DESTINATIONS_AT = 3000;

	public static final String BATCH_CALCULATION_SAMPLE_JSON = "{\"profile\":\"Wohnen\",\"sources\":[{\"id\":1,\"name\":\"Location1\",\"lat\":48.2061121370655,\"lon\":16.3724265546418},{\"id\":2,\"name\":\"Location2\",\"lat\":48.2042327131692,\"lon\":16.3695610097329}]}";
	
}
