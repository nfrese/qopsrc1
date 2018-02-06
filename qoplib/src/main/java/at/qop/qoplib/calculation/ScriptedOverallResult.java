package at.qop.qoplib.calculation;

import java.util.List;

import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import at.qop.qoplib.GLO;

public class ScriptedOverallResult<T extends ILayerCalculation> extends OverallResult<T> {
	
	public final String func;
	
	public ScriptedOverallResult(String func, List<CalculationSection<T>> sections) {
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
