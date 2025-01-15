package at.qop.ws;

import at.qop.qoplib.Config;

public abstract class QOPRestApiBase {
	
	protected Config checkAuth(String username, String password) {
		if (username == null) throw new RuntimeException("URL parameter username required");
		if (password == null) throw new RuntimeException("URL parameter password required");
		
		Config cfg = Config.read();
		if (!password.equals(cfg.getUserPassword(username)))
		{
			throw new RuntimeException("Invalid username/password!");
		}
		return cfg;
	}

}
