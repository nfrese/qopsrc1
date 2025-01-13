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

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class EnterTextDialog extends AbstractDialog {
	
	private static final long serialVersionUID = 1L;
	
	private String text;

	private ValueChangeListener<ValueChangeEvent<String>> cl;
	
	public EnterTextDialog(String title, String message)
	{
		super(title);
		this.setModal(true);
		this.text = message;

		Dialog subWindow = this;
    	subWindow.setModal(true);
        VerticalLayout subContent = new VerticalLayout();
        subContent.setSpacing(true);
        subWindow.add(subContent);

        TextField profileName = new TextField(text);
        profileName.focus();
        
		subContent.add(profileName);
        Button okButton = new Button("OK", VaadinIcon.CHECK.create());
        //okButton.setClickShortcut(KeyCode.ENTER); TODO
		okButton.addClickListener(e2 -> {
			if (cl != null) cl.valueChanged(new ValueChangeEvent<String>() {

				private static final long serialVersionUID = 1L;

				@Override
				public HasValue getHasValue() {
					return profileName;
				}

				@Override
				public boolean isFromClient() {
					return false;
				}

				@Override
				public String getOldValue() {
					return null;
				}

				@Override
				public String getValue() {
					return text;
				}
				//    (profileName, null, true));
			});
			this.close(); 
		});
		Button cancelButton = new Button("Abbruch", VaadinIcon.CLOSE.create());
		cancelButton.addClickListener(e2 -> {
			this.close(); 
		});
        
        subContent.add(okButton);
        subContent.add(cancelButton);
	}
	
	public EnterTextDialog ok(ValueChangeListener<ValueChangeEvent<String>> cl)
	{
		this.cl = cl;
		return this;
	}

}
