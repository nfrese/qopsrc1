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

import java.util.List;

import at.qop.qoplib.Config;
import at.qop.qoplib.Constants;
import at.qop.qoplib.calculation.DbLayerSource;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.osrmclient.OSRMClient;

public class BatchCalculationInMemoryImpl extends BatchCalculationInMemory {

	public BatchCalculationInMemoryImpl(Profile currentProfile, List<Address> addresses) {
		super(currentProfile, addresses);
	}

	@Override
	protected OSRMClient initRouter() {
		Config cf = Config.read();
		return new OSRMClient(cf.getOSRMConf(), Constants.SPLIT_DESTINATIONS_AT);
	}
	
	@Override
	protected DbLayerSource initSource() {
		return new DbLayerSource();
	}

}
