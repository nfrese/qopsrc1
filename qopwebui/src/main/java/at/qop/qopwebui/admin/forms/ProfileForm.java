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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

import at.qop.qoplib.entities.Profile;
import at.qop.qopwebui.components.JsValidator;

public class ProfileForm extends AbstractForm {
	
	private static final long serialVersionUID = 1L;
	
	private final Profile profile;
	private Binder<Profile> binder;
	
	public ProfileForm(String title, Profile profile, boolean create) {
		super(title, create);
		this.profile = profile;
		binder.readBean(profile);
	}
	
	@Override
	protected Component initComponents(boolean create) {

		VerticalLayout vl = new VerticalLayout();

		binder = new Binder<>();
		
		{
			TextField textField = new TextField("Profilename");
			textField.setEnabled(create);
			vl.add(textField);
			binder.bind(textField, o -> o.name, (o,v) -> o.name = v);
		}	
		{
			TextField textField = new TextField("Beschreibung");
			vl.add(textField);
			binder.bind(textField, o -> o.description, (o,v) -> o.description = v);
		}	
		{
			TextArea textArea = new TextArea("Auswertungs-Funktion (Javascript)");
			textArea.setWidth(600, Unit.PIXELS);
			textArea.setHeight(200, Unit.PIXELS);
			vl.add(textArea);
			binder.forField(textArea).withValidator(new JsValidator()).bind(o -> o.aggrfn, (o,v) -> o.aggrfn = v);
		}
		return vl;
	}

	@Override
	protected void saveData() {
		try {
			binder.writeBean(profile);
		} catch (ValidationException e) {
			new Notification("Validation error count: "
					+ e.getValidationErrors().size()).open();
		}
	}
	

}
