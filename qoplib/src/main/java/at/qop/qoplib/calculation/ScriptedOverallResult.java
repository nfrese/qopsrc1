package at.qop.qoplib.calculation;

import java.util.List;

import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import at.qop.qoplib.GLO;

public class ScriptedOverallResult extends OverallResult {
	
	public final String func;
	
	public ScriptedOverallResult(String func, List<CalculationSection> sections) {
		super(sections);
		this.func = func;
	}
	
	public void _run()
	{
		ScriptContext context = new SimpleScriptContext();
		context.setAttribute("ca", this, ScriptContext.ENGINE_SCOPE);
		try {
			GLO.get().jsEngine.eval(func, context);
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
	}

}
