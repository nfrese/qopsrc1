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
		try {
			Properties props = new Properties();
			props.put("java.naming.factory.url.pkgs","org.jboss.ejb.client.naming");
			InitialContext context = new InitialContext(props);

			String beanName = beanClass.getSimpleName();        	 
			String name = "java:global/qopear/qoplib/" + beanName;
			@SuppressWarnings("unchecked")
			B bean = (B)context.lookup(name);
			return bean;
		} catch (NamingException e)
		{
			throw new RuntimeException(e);
		}
	}

}
