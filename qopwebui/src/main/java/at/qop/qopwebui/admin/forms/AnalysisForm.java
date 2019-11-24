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

import java.util.Arrays;
import java.util.List;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationException;
import com.vaadin.server.Page;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.entities.Analysis;
import at.qop.qoplib.entities.AnalysisFunction;
import at.qop.qoplib.entities.ModeEnum;
import at.qop.qopwebui.components.JsValidator;

public class AnalysisForm extends AbstractForm {
	
	private static final long serialVersionUID = 1L;
	
	final Analysis analysis;
	private Binder<Analysis> binder;
	
	public AnalysisForm(String title, Analysis analysis, boolean create) {
		super(title, create);
		this.analysis = analysis;
		binder.readBean(analysis);
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
			TextField textField = new TextField("SQL");
			textField.setWidth(600, Unit.PIXELS);
			vl.addComponent(textField);
			binder.forField(textField).withValidator(new AnalysisQueryValidator(this)).bind(o -> o.query, (o,v) -> o.query = v);
		}	
		{
			ComboBox<ModeEnum> modeCombo = new ComboBox<>("Routing Modus", Arrays.asList(ModeEnum.values()));
			modeCombo.setTextInputAllowed(false);
			vl.addComponent(modeCombo);
			binder.bind(modeCombo, o -> o.mode, (o,v) -> o.mode = v);
		}
		{
			TextField textField = new TextField("Geometrie-Feld");
			vl.addComponent(textField);
			binder.bind(textField, o -> o.geomfield, (o,v) -> o.geomfield = v);
		}
		{
			List<AnalysisFunction> funcs = LookupSessionBeans.profileDomain().listAnalysisFunctions();
			ComboBox<AnalysisFunction> funcCombo = new ComboBox<>("Auswertungsfunktion", funcs);
			funcCombo.setEmptySelectionAllowed(false);
			funcCombo.setTextInputAllowed(false);
			funcCombo.setWidth(500, Unit.PIXELS);
			vl.addComponent(funcCombo);
			binder.bind(funcCombo, o -> o.analysisfunction, (o,v) -> o.analysisfunction = v);
		}	
		{
			TextArea textArea = new TextArea("Rating-Funktion (Javascript)");
			textArea.setWidth(600, Unit.PIXELS);
			textArea.setHeight(80, Unit.PIXELS);
			vl.addComponent(textArea);
			binder.forField(textArea).withValidator(new JsValidator()).bind(o -> o.ratingfunc, (o,v) -> o.ratingfunc = v);
		}
		{
			TextField textField = new TextField("Radius Objektfilterung (0 bedeutet keine Einschränkung)");
			vl.addComponent(textField);
			binder.bind(textField, o -> o.radius + "", (o,v) -> o.radius = Double.parseDouble(v));
		}
		
		return vl;
	}

	@Override
	protected void saveData() {
		try {
			binder.writeBean(analysis);
		} catch (ValidationException e) {
			new Notification("Validation error count: "
					+ e.getValidationErrors().size()).show((UI.getCurrent().getPage()));
		}
	}
	

}
