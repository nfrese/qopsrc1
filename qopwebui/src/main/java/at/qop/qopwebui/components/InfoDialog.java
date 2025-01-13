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

package at.qop.qopwebui.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class InfoDialog extends ConfirmationDialog {
	
	public InfoDialog(String title, String message) {
		super(title, message);
	}

	private static final long serialVersionUID = 1L;

	public VaadinIcon icon() {
		return VaadinIcon.INFO;
	}
	
	public HorizontalLayout buttons() {
		Button okButton = new Button("OK", VaadinIcon.CHECK.create());
		okButton.addClickListener(e2 -> {
			if (cl != null) cl.buttonClick(null);
			this.close(); 
		});
		HorizontalLayout buttons = new HorizontalLayout(okButton);
		return buttons;
	}

}
