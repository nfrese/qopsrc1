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

import java.io.PrintWriter;
import java.io.StringWriter;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

public class ExceptionDialog extends AbstractDialog {
	
	private static final long serialVersionUID = 1L;
	
	private String text;

	public ExceptionDialog(String title, String message)
	{
		super(title);
		this.setModal(true);
		this.setWidth(640, Unit.PIXELS);
		this.setHeight(480, Unit.PIXELS);
		this.text = message;
		VerticalLayout subContent = new VerticalLayout();
		subContent.setSizeFull();
		this.add(subContent);
		
		TextArea textArea = new TextArea();
		textArea.setSizeFull();
		textArea.setValue(text);
		subContent.add(VaadinIcon.EXCLAMATION_CIRCLE.create());
		
		subContent.add(textArea);
		//subContent.setExpandRatio(textArea, 10.0f); TODO
		Button cancelButton = new Button("OK", VaadinIcon.CHECK.create());
		cancelButton.addClickListener(e2 -> {
			this.close(); 
		});
		subContent.add(new HorizontalLayout(cancelButton));

	}
	
	public ExceptionDialog(String title, Throwable t) {
		this(title, exception2string(t));
	}

	public static String exception2string(Throwable t) {
		t.printStackTrace();
		StringWriter pw = new StringWriter();
		t.printStackTrace(new PrintWriter(pw));
		return pw.toString();
	}

}
