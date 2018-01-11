package at.qop.qoplib.entities;

import java.io.Serializable;

public enum ModeEnum implements Serializable {
	
	car("PKW", "driving", 0),
	bike("Fahrrad", "bike", 1), 
	foot("Fußgänger", "foot", 2),
	air("Luftlinie (kein Routing)", null, -1);
	
	public final String desc;
	public final String osrmProfile;
	public final int osrmPortOffset;

	ModeEnum(String desc, String osrmProfile, int osrmPortOffset)
	{
		this.desc = desc;
		this.osrmProfile = osrmProfile;
		this.osrmPortOffset = osrmPortOffset;
	}
	
	@Override
	public String toString() {
		return desc;
	}

}
