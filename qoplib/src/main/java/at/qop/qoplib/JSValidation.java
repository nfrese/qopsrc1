package at.qop.qoplib;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class JSValidation {

	public static boolean check(String js)
	{
		ScriptEngine engine = GLO.get().jsEngine;
		try {
			CompiledScript compiled = ((Compilable) engine).compile(js);
		} catch (ScriptException e) {
			return false;
		}
		return true;
	}
	
	public static String getMessage(String js)
	{
		ScriptEngine engine = GLO.get().jsEngine;
		try {
			CompiledScript compiled = ((Compilable) engine).compile(js);
		} catch (ScriptException e) {
			return e.getMessage();
		}
		return null;
	}
	
}
