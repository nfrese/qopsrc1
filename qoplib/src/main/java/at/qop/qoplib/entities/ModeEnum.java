package at.qop.qoplib.entities;

import java.io.Serializable;

public enum ModeEnum implements Serializable {
	
	car("PKW", "driving"),
	bike("Fahrrad", "bike"), 
	foot("Fußgänger", "foot"),
	air("Luftlinie", null);
	
	public final String desc;
	public final String osrmProfile;

	ModeEnum(String desc, String osrmProfile)
	{
		this.desc = desc;
		this.osrmProfile = osrmProfile;
	}
	
	@Override
	public String toString() {
		return desc;
	}

}
