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

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

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
		this.setContent(subContent);
		
		TextArea textArea = new TextArea();
		textArea.setSizeFull();
		textArea.setValue(text);
		textArea.setIcon(VaadinIcons.EXCLAMATION_CIRCLE);
		
		subContent.addComponent(textArea);
		subContent.setExpandRatio(textArea, 10.0f);
		Button cancelButton = new Button("OK", VaadinIcons.CHECK);
		cancelButton.addClickListener(e2 -> {
			this.close(); 
		});
		subContent.addComponent(new HorizontalLayout(cancelButton));

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
