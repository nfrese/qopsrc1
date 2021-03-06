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

package at.qop.qopwebui.admin.forms;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationException;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import at.qop.qoplib.entities.AnalysisFunction;
import at.qop.qopwebui.components.JsValidator;

public class AnalysisFunctionForm extends AbstractForm {
	
	private static final long serialVersionUID = 1L;
	
	private final AnalysisFunction analysisFunction;
	private Binder<AnalysisFunction> binder;
	
	public AnalysisFunctionForm(String title, AnalysisFunction analysisFunction, boolean create) {
		super(title, create);
		this.analysisFunction = analysisFunction;
		binder.readBean(analysisFunction);
	}
	
	@Override
	protected Component initComponents(boolean create) {

		VerticalLayout vl = new VerticalLayout();

		binder = new Binder<>();
		{
			TextField textField = new TextField("Name");
			textField.setWidth(450, Unit.PIXELS);
			textField.setEnabled(create);
			vl.addComponent(textField);
			binder.bind(textField, o -> o.name, (o,v) -> o.name = v);
		}	
		{
			TextField textField = new TextField("Beschreibung");
			textField.setWidth(600, Unit.PIXELS);
			vl.addComponent(textField);
			binder.bind(textField, o -> o.description, (o,v) -> o.description = v);
		}	
		{
			TextArea textArea = new TextArea("Auswertungs-Funktion (Javascript)");
			textArea.setWidth(640, Unit.PIXELS);
			textArea.setHeight(200, Unit.PIXELS);
			vl.addComponent(textArea);
			binder.forField(textArea).withValidator(new JsValidator()).bind(o -> o.func, (o,v) -> o.func = v);
		}
		{
			TextField textField = new TextField("Einheit Resultat (zb Minuten, Meter,...)");
			vl.addComponent(textField);
			binder.bind(textField, o -> o.rvalUnit, (o,v) -> o.rvalUnit = v);
		}
		
		return vl;
	}

	@Override
	protected void saveData() {
		try {
			binder.writeBean(analysisFunction);
		} catch (ValidationException e) {
			new Notification("Validation error count: "
					+ e.getValidationErrors().size()).show((Page)this.getParent());
		}
	}
	

}
