package at.qop.qoplib;

import java.util.List;
import java.util.Optional;

import at.qop.qoplib.domains.IConfigDomain;
import at.qop.qoplib.entities.Config;

public class Configuration {
	
	public static String TITLE() { 
		
		IConfigDomain cd;
		cd = LookupSessionBeans.configDomain();
		
		List<Config> confs = cd.readConfiguration();
		
		Optional<Config> optTitle = confs.stream().filter(e -> e.k.equalsIgnoreCase("title")).findFirst();
		if (optTitle.isPresent()) return optTitle.get().value;
		
		
		return "Hell World"; 
	}

}
