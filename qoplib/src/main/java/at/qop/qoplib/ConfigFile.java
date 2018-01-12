package at.qop.qoplib;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ConfigFile {
	
	private final Properties props;
	
	public ConfigFile(Properties props) {
		this.props = props;
	}

	public static ConfigFile read() {
		Properties props = new Properties();
		String fileName = System.getProperty("jboss.server.config.dir") + "/qop.properties";
		try(FileInputStream fis = new FileInputStream(fileName)) {
		  props.load(fis);
		} catch (FileNotFoundException e) {
			System.out.println(e + " -> using defaults!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ConfigFile(props);
	}
	
	public String getOSRMHost()
	{
		String key = "OSRMHost";
		String defaultValue = "localhost";
		return getStrProp(key, defaultValue);
	}

	public int getOSRMPort()
	{
		String key = "OSRMPort";
		String defaultValue = "5000";
		return Integer.valueOf(getStrProp(key, defaultValue));
	}
	
	private String getStrProp(String key, String defaultValue) {
		if (props.contains(key)) return props.getProperty(key);
		else return defaultValue;
	}
	
	
	

}
