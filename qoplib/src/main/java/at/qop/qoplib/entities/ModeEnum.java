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

package at.qop.qoplib.entities;

import java.io.Serializable;

public enum ModeEnum implements Serializable {
	
	car("PKW", "driving", 0),
	bike("Fahrrad", "bike", 1), 
	foot("Fußgänger", "foot", 2),
	air("Luftlinie (kein Routing)", null, -1);
	
	public final String desc;
	public final String osrmProfile;
	public final int osrmPortOffset;

	ModeEnum(String desc, String osrmProfile, int osrmPortOffset)
	{
		this.desc = desc;
		this.osrmProfile = osrmProfile;
		this.osrmPortOffset = osrmPortOffset;
	}
	
	@Override
	public String toString() {
		return desc;
	}

}
