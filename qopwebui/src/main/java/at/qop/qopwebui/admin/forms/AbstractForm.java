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

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public abstract class AbstractForm extends Window {

	private static final long serialVersionUID = 1L;
	
	private ClickListener cl;

	public AbstractForm(String title, boolean create)
	{
		super(title);
		this.setModal(true);
		Component c = initComponents(create);
		VerticalLayout subContent = new VerticalLayout();
		subContent.addComponent(c);
		this.setContent(subContent);
		
		Button okButton = new Button("Speichern", VaadinIcons.CHECK);
		okButton.addClickListener(e2 -> {
			saveData();
			if (cl != null) cl.buttonClick(null);
			this.close();
		});
		
		Button cancelButton = new Button("Abbruch", VaadinIcons.CLOSE);
		cancelButton.addClickListener(e2 -> {
			this.close(); 
		});
		subContent.addComponent(new HorizontalLayout(okButton, cancelButton));
	}

	public void show()
	{
		this.center();
		UI.getCurrent().addWindow(this);
	}
	
	public AbstractForm ok(ClickListener cl)
	{
		this.cl = cl;
		return this;
	}
	
	protected abstract void saveData();

	protected abstract Component initComponents(boolean create);
}
