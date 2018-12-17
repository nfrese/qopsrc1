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

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class EnterTextDialog extends AbstractDialog {
	
	private static final long serialVersionUID = 1L;
	
	private String text;

	private ValueChangeListener<String> cl;
	
	public EnterTextDialog(String title, String message)
	{
		super(title);
		this.setModal(true);
		this.text = message;

       	Window subWindow = this;
    	subWindow.setModal(true);
        VerticalLayout subContent = new VerticalLayout();
        subContent.setSpacing(true);
        subWindow.setContent(subContent);

        TextField profileName = new TextField(text);
        profileName.focus();
        
		subContent.addComponent(profileName);
        Button okButton = new Button("OK", VaadinIcons.CHECK);
        okButton.setClickShortcut(KeyCode.ENTER);
		okButton.addClickListener(e2 -> {
			if (cl != null) cl.valueChange(new ValueChangeEvent<String>(profileName, null, true));
			this.close(); 
		});
		Button cancelButton = new Button("Abbruch", VaadinIcons.CLOSE);
		cancelButton.addClickListener(e2 -> {
			this.close(); 
		});
        
        subContent.addComponent(okButton);
        subContent.addComponent(cancelButton);
	}
	
	public EnterTextDialog ok(ValueChangeListener<String> cl)
	{
		this.cl = cl;
		return this;
	}

}
