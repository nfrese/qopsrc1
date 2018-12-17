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

package at.qop.qoplib.dbconnector.fieldtypes;

import java.util.Arrays;

import at.qop.qoplib.dbconnector.DbTable;

/**
 * see https://www.postgresql.org/message-id/AANLkTikkkxN%2B-UUiGVTzj8jdfS4PdpB8_tDONMFHNqHk%40mail.gmail.com
 */
public abstract class DbFieldAbstract {
	
	public DbTable table;
	public int ix;
	public String name;
	
	public void checkFieldType(String typeName) {
		boolean found = false;
		String[] exps = expectedTypeName();
		for (String exp : exps) {
			if (exp.equalsIgnoreCase(typeName)) found=true;
		}
		if (!found) throw new IllegalArgumentException("field " + name + " expected types:" + Arrays.toString(exps) + " but got " + typeName);
	}

	protected abstract String[] expectedTypeName();
	
}
