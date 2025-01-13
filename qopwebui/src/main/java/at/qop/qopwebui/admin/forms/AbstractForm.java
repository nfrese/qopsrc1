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
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.ValidationException;

import at.qop.qopwebui.components.ClickListener;

public abstract class AbstractForm extends Dialog {

	private static final long serialVersionUID = 1L;
	
	private ClickListener cl;

	public AbstractForm(String title, boolean create)
	{
		super(title);
		this.setModal(true);
		Component c = initComponents(create);
		VerticalLayout subContent = new VerticalLayout();
		subContent.add(c);
		this.add(subContent);
		
		Label validationMessage=new Label();
		
		Button okButton = new Button("Speichern", VaadinIcon.CHECK.create());
		okButton.addClickListener(e2 -> {
			try {
				saveData();
			} catch (Exception e) {
				validationMessage.setText(e.getMessage());
				e.printStackTrace();
				return;
			}
			if (cl != null) cl.buttonClick(null);
			this.close();
		});
		
		Button cancelButton = new Button("Abbruch", VaadinIcon.CLOSE.create());
		cancelButton.addClickListener(e2 -> {
			this.close(); 
		});
		subContent.add(new HorizontalLayout(okButton, cancelButton, validationMessage));
	}

	public void show()
	{
		//this.center();
		UI.getCurrent().add(this);
	}
	
	public AbstractForm ok(ClickListener cl)
	{
		this.cl = cl;
		return this;
	}
	
	protected abstract void saveData() throws ValidationException;

	protected abstract Component initComponents(boolean create);
}
