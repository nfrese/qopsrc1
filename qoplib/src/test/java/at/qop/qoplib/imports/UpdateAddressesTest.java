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

package at.qop.qoplib.imports;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class UpdateAddressesTest {
	
	int cnt = 0;
	@Test
	public void test() throws IOException {
		
		cnt = 0;
		UpdateAddresses updateAddresses = new UpdateAddresses("01");
		updateAddresses.onPacket(p -> {
			
			System.out.println(p);
			cnt+= p.records().size();
			return null;
		});
		updateAddresses.runUpdate();
		updateAddresses.done();
		
		System.out.println(cnt);
		System.out.println(updateAddresses.recordCount);
	}


}
