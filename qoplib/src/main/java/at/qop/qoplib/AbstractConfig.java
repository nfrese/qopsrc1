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

public abstract class AbstractConfig implements Config {

	@Override
	public OSRMConf getOSRMConf()
	{
		OSRMConf osrmConf = new OSRMConf();
		
		{
			String key = "OSRMHost";
			if (containsKey(key))
			{
				String host = getStrProp(key, osrmConf.carHost);
				osrmConf.carHost = host;
				osrmConf.bicycleHost = host;
				osrmConf.footHost = host;
			}
		}
		
		{
			String key = "OSRMPort";
			if (containsKey(key))
			{
				Integer startPort = Integer.valueOf(getStrProp(key, osrmConf.carPort+""));
				osrmConf.carPort = startPort;
				osrmConf.bicyclePort = startPort+1;
				osrmConf.footPort = startPort+2;
			}
		}
		
		{
			osrmConf.carHost = getStrProp("OSRMHost_car", osrmConf.carHost);
			osrmConf.bicycleHost = getStrProp("OSRMHost_bicycle", osrmConf.bicycleHost);
			osrmConf.footHost = getStrProp("OSRMHost_foot", osrmConf.footHost);
			
			{
				String k = "OSRMPort_car";
				if (containsKey(k)) osrmConf.carPort = Integer.valueOf(getStrProp(k, osrmConf.carPort+""));
			}
			{
				String k = "OSRMPort_bicycle";
				if (containsKey(k)) osrmConf.bicyclePort = Integer.valueOf(getStrProp(k, osrmConf.bicyclePort+""));
			}
			{
				String k = "OSRMPort_foot";
				if (containsKey(k)) osrmConf.footPort = Integer.valueOf(getStrProp(k, osrmConf.footPort+""));
			}
			return osrmConf;
		}
	}
	
	protected abstract boolean containsKey(String key);

	protected abstract String getStrProp(String key, String defaultValue);
	
	public boolean hasUser(String username)
	{
		return getUserPassword(username) != null;
	}
	
	@Override
	public String getUserPassword(String username)
	{
		String defaultValue = null;
		return getStrProp("password." + username, defaultValue);
	}
	
	@Override
	public boolean isAdmin(String username)
	{
		String defaultValue = "false";
		return "true".equalsIgnoreCase(getStrProp("isadmin." + username, defaultValue));
	}

	@Override
	public String[] getUserProfiles(String username)
	{
		String defaultValue = null;
		String value = getStrProp("userprofiles." + username, defaultValue);
		if (value == null) return null;
		return value.split(",");
	}
	
	@Override
	public String getWorkingDir()
	{
		String key = "workingdir";
		String defaultValue = "/tmp";
		return getStrProp(key, defaultValue);
	}

	@Override
	public String getDbHost() {
		String key = "dbhost";
		String defaultValue = "localhost";
		return getStrProp(key, defaultValue);
	}

	@Override
	public String getDbUserName() {
		String key = "dbuser";
		String defaultValue = "qopuser";
		return getStrProp(key, defaultValue);
	}

	@Override
	public String getDb() {
		String key = "db";
		String defaultValue = "qop";
		return getStrProp(key, defaultValue);
	}

	@Override
	public int getPort() {
		String key = "dbport";
		String defaultValue = "5432";
		return Integer.valueOf(getStrProp(key, defaultValue));
	}

	@Override
	public String getDbPasswd() {
		String key = "dbpasswd";
		String defaultValue = "unknown";
		return getStrProp(key, defaultValue);
	}

	@Override
	public String getAddressLookupURL() {
		String key = "addresslookupbaseurl";
		String defaultValue = "https://nominatim.openstreetmap.org/search?format=json&q="; // demo
		return getStrProp(key, defaultValue);
	}
	
	@Override
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
	
	@Override
	public String toString() {
		return "[getOSRMConf()=" + getOSRMConf() 
			+ ",\n getWorkingDir()=" + getWorkingDir()
			+ ",\n getDbHost()=" + getDbHost() 
			+ ",\n getDbUserName()=" + getDbUserName() 
			+ ",\n getDbPassword()=" + getDbPasswd().replaceAll(".", "*")
			+ ",\n getDb()=" + getDb()
			+ ",\n getPort()=" + getPort() + "]\n";
	}
}
