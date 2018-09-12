package at.qop.qoplib;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Properties;
import java.util.TreeSet;

import at.qop.qoplib.entities.Profile;

public class ConfigFile {
	
	private final Properties props;
	
	public ConfigFile(Properties props) {
		this.props = props;
	}

	public static ConfigFile read() {
		Properties props = new Properties();
		String fileName = System.getProperty("jboss.server.config.dir") + "/qop.properties";
		try(FileInputStream fis = new FileInputStream(fileName)) {
		  props.load(new InputStreamReader(fis, "UTF-8"));
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
		if (props.containsKey(key)) return props.getProperty(key);
		else return defaultValue;
	}
	
	public boolean hasUser(String username)
	{
		return getUserPassword(username) != null;
	}
	
	public String getUserPassword(String username)
	{
		String defaultValue = null;
		return getStrProp("password." + username, defaultValue);
	}
	
	public boolean isAdmin(String username)
	{
		String defaultValue = "false";
		return "true".equalsIgnoreCase(getStrProp("isadmin." + username, defaultValue));
	}

	public String[] getUserProfiles(String username)
	{
		String defaultValue = null;
		String value = getStrProp("userprofiles." + username, defaultValue);
		if (value == null) return null;
		return value.split(",");
	}
	
	public String getWorkingDir()
	{
		String key = "workingdir";
		String defaultValue = "/tmp";
		return getStrProp(key, defaultValue);
	}

	public String getDbHost() {
		String key = "dbhost";
		String defaultValue = "localhost";
		return getStrProp(key, defaultValue);
	}

	public String getDbUserName() {
		String key = "dbuser";
		String defaultValue = "qopuser";
		return getStrProp(key, defaultValue);
	}

	public String getDb() {
		String key = "db";
		String defaultValue = "qop";
		return getStrProp(key, defaultValue);
	}

	public int getPort() {
		String key = "dbport";
		String defaultValue = "5432";
		return Integer.valueOf(getStrProp(key, defaultValue));
	}

	public String getDbPasswd() {
		String key = "dbpasswd";
		String defaultValue = "unknown";
		return getStrProp(key, defaultValue);
	}

	public void checkUserProfile(String username, String profileName) {
		String[] ups = getUserProfiles(username);
		if (ups != null)
		{
			for (String name : ups)
			{
				if (name.equals(profileName))
				{
					return;
				}
			}
		}
		throw new RuntimeException("No permission to query profile: "+ profileName);
	}
}
