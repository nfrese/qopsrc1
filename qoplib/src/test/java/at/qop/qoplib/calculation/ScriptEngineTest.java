package at.qop.qoplib.calculation;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.junit.Assert;
import org.junit.Test;

import at.qop.qoplib.GLO;
import at.qop.qoplib.JSValidation;

public class ScriptEngineTest {
	
	private static final String FUNCTIONS =
		    "function() {" +
		    		"  return \"Hello\";" +
		    		"}";

	@Test
	public void test() throws ScriptException
	{
		ScriptEngine e = GLO.get().jsEngine;

		ScriptEngine engine = new ScriptEngineManager().getEngineByMimeType("text/javascript");

		CompiledScript compiled = ((Compilable) engine).compile(FUNCTIONS);
		Object sayHello = compiled.eval();

		SimpleBindings global = new SimpleBindings();
		global.put("sayHello", sayHello);
		String script = "sayHello()";
		System.out.println(engine.eval(script, global));
	}

	@Test
	public void testValidationOk() {
		String r = JSValidation.getMessage("5*4");
		Assert.assertNull(r);
	}

	@Test
	public void testValidationFailed() {
		String r = JSValidation.getMessage("Hello World;");
		Assert.assertEquals("<eval>:1:6 Expected ; but found World\n" + 
				"Hello World;\n" + 
				"      ^ in <eval> at line number 1 at column number 6", r);
	}
	
}
