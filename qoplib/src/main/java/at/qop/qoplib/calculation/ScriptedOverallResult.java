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
