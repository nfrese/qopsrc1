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

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import at.qop.qoplib.domains.AddressDomain;
import at.qop.qoplib.domains.ConfigDomain;
import at.qop.qoplib.domains.GenericDomain;
import at.qop.qoplib.domains.IAddressDomain;
import at.qop.qoplib.domains.IConfigDomain;
import at.qop.qoplib.domains.IGenericDomain;
import at.qop.qoplib.domains.IProfileDomain;
import at.qop.qoplib.domains.ProfileDomain;

public class LookupSessionBeans {

	public static IConfigDomain configDomain() {
		return lookupDomain(ConfigDomain.class);
	}

	public static IAddressDomain addressDomain() {
		return lookupDomain(AddressDomain.class);
	}

	public static IProfileDomain profileDomain() {
		return lookupDomain(ProfileDomain.class);
	}
	
	public static IGenericDomain genericDomain() {
		return lookupDomain(GenericDomain.class);
	}	
	
	public static <B> B lookupDomain(Class<B> beanClass) {
		
		String appName = Utils.getApplicationName();
		
		try {
			Properties props = new Properties();
			props.put("java.naming.factory.url.pkgs","org.jboss.ejb.client.naming");
			InitialContext context = new InitialContext(props);

			String beanName = beanClass.getSimpleName();        	 
			String name = "java:global/" + appName + "/qoplib/" + beanName;
			@SuppressWarnings("unchecked")
			B bean = (B)context.lookup(name);
			return bean;
		} catch (NamingException e)
		{
			throw new RuntimeException(e);
		}
	}

}
