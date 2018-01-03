package at.qop.qoplib;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import at.qop.qoplib.domains.ConfigDomain;
import at.qop.qoplib.domains.IConfigDomain;

public class LookupDomains {

	public static IConfigDomain configDomain() {
		try {
			Properties props = new Properties();
			props.put("java.naming.factory.url.pkgs","org.jboss.ejb.client.naming");
			InitialContext context = new InitialContext(props);

			String appName = "";        	 
			String moduleName = "MyAdditionEJB";
			String distinctName = "";        	 
			String beanName = ConfigDomain.class.getSimpleName();        	 
			String interfaceName = IConfigDomain.class.getName();
			String name = "ejb:" + appName + "/" + moduleName + "/" +  distinctName    + "/" + beanName + "!" + interfaceName;
			name = "java:app/qoplib/ConfigDomain";
			name = "java:global/qopear/qoplib/ConfigDomain";
			System.out.println(name);
			IConfigDomain bean = (IConfigDomain)context.lookup(name);
			return bean;
		} catch (NamingException e)
		{
			throw new RuntimeException(e);
		}
	}

}
