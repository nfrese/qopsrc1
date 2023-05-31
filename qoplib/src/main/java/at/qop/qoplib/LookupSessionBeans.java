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

import org.springframework.context.ApplicationContext;

import at.qop.qoplib.domains.AddressDomain;
import at.qop.qoplib.domains.ConfigDomain;
import at.qop.qoplib.domains.GenericDomain;
import at.qop.qoplib.domains.IAddressDomain;
import at.qop.qoplib.domains.IConfigDomain;
import at.qop.qoplib.domains.IGenericDomain;
import at.qop.qoplib.domains.IProfileDomain;
import at.qop.qoplib.domains.ProfileDomain;

public class LookupSessionBeans {
	
	public static ApplicationContext applicationContextStatic;
	
	public static boolean inTestContext = false;
	public static String jdbcTestConnectionUrl;
	public static String jdbcTestConnectionUserName;
	public static String jdbcTestConnectionPassword;	

	public static IConfigDomain configDomain() {
		return applicationContextStatic.getBean(ConfigDomain.class);
	}

	public static IAddressDomain addressDomain() {
		return applicationContextStatic.getBean(AddressDomain.class);
	}

	public static IProfileDomain profileDomain() {
		return applicationContextStatic.getBean(ProfileDomain.class);
	}
	
	public static IGenericDomain genericDomain() {
		return applicationContextStatic.getBean(GenericDomain.class);
	}	

}
