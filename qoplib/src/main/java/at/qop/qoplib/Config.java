package at.qop.qoplib;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

import at.qop.qoplib.entities.ModeEnum;

public interface Config {
	
	public static class OSRMConf
	{
		public static final String DEFAULT_HOST="localhost";
		
		String carHost = DEFAULT_HOST;
		int carPort = 5300;
		String bicycleHost = DEFAULT_HOST;
		int bicyclePort = 5301;
		String footHost = DEFAULT_HOST;
		int footPort = 5302;
		
		public String baseUrl(ModeEnum mode) {
			String host;
			int port = -1;
			
			switch (mode) {
				case car:
					host = carHost;
					port = carPort;
					break;
				case bike:
					host = bicycleHost;
					port = bicyclePort;
					break;
				case foot:
					host = footHost;
					port = footPort;
					break;
				default: 
					throw new RuntimeException("Unexcpected mode for OSRM base URL " + mode);
			}
			return "http://" + host + ":" + port;
		}
		
		@Override
		public String toString() {
			return "OSRMConf [carHost=" + carHost + ", carPort=" + carPort + ", bicycleHost=" + bicycleHost
					+ ", bicyclePort=" + bicyclePort + ", footHost=" + footHost + ", footPort=" + footPort + "]";
		}

	}

	OSRMConf getOSRMConf();

	String getUserPassword(String username);

	String[] getUserProfiles(String username);

	String getWorkingDir();

	String getDbHost();

	String getDbUserName();

	String getDb();

	int getPort();

	String getDbPasswd();
	
	boolean isAdmin(String username);
	
	boolean hasUser(String username);
	
	void checkUserProfile(String username, String profileName);

	static ArrayList<String> alreadyPrinted = new ArrayList<>();
	
	static Config read() {
		Config config = readFromProperties();
		if (alreadyPrinted.size() == 0)
		{
			System.out.println("*** USING CONFIGURATION: " + config);
			alreadyPrinted.add("yes");
		}
		return config;
	}

	static Config readFromProperties() {
		String appName = Utils.getApplicationName();
		
		Properties props = new Properties();
		String fileName = System.getProperty("jboss.server.config.dir") + "/" + appName + ".properties";
		try(FileInputStream fis = new FileInputStream(fileName)) {
		  props.load(new InputStreamReader(fis, "UTF-8"));
		} catch (FileNotFoundException e) {
			System.out.println(e + " -> using environment or defaults!");
			return readFromEnvir();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ConfigFile(props);
	}

	static Config readFromEnvir() {
		return new ConfigEnvir();
	}

}
