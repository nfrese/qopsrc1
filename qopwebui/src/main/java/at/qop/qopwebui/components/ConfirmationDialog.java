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

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class ConfirmationDialog extends AbstractDialog {
	
	private static final long serialVersionUID = 1L;
	
	private String text;

	protected ClickListener cl;
	
	public ConfirmationDialog(String title, String message)
	{
		super(title);
		this.setModal(true);
		this.text = message;
		VerticalLayout subContent = new VerticalLayout();
		this.setContent(subContent);
		
		Label msgLabel = new Label(text);
		msgLabel.setIcon(icon());
		
		subContent.addComponent(msgLabel);
		HorizontalLayout buttons = buttons();
		subContent.addComponent(buttons);

	}

	public VaadinIcons icon() {
		return VaadinIcons.QUESTION;
	}

	public HorizontalLayout buttons() {
		Button okButton = new Button("OK", VaadinIcons.CHECK);
		okButton.addClickListener(e2 -> {
			if (cl != null) cl.buttonClick(null);
			this.close(); 
		});
		Button cancelButton = new Button("Abbruch", VaadinIcons.CLOSE);
		cancelButton.addClickListener(e2 -> {
			this.close(); 
		});
		HorizontalLayout buttons = new HorizontalLayout(okButton, cancelButton);
		return buttons;
	}
	
	public ConfirmationDialog ok(ClickListener cl)
	{
		this.cl = cl;
		return this;
	}

}
