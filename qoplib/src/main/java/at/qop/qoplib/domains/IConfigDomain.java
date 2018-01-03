package at.qop.qoplib.domains;

import java.util.List;

import at.qop.qoplib.entities.Config;

public interface IConfigDomain {
	
	List<Config> readConfiguration();

}
