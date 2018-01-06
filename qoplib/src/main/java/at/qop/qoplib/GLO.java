package at.qop.qoplib;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class GLO {
	
	private static GLO singleton = null; 
	
	public static GLO get() {
		if (singleton == null) {
			singleton = new GLO();
		}
		return singleton;
	}
	
	
	public ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("nashorn");

}
