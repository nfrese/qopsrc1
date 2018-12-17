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

import java.util.List;

import at.qop.qoplib.batch.BatchCalculationInMemoryImpl;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Profile;

public class BatchHandlerImpl extends BatchHandler {

	@Override
	protected Profile lookupProfile(String profile) {
		// TODO Auto-generated method stub
		return null;
	}

	protected BatchCalculationInMemoryImpl createBC(Profile profile, List<Address> addresses) {
		return new BatchCalculationInMemoryImpl(profile, addresses);
	}
	
}
