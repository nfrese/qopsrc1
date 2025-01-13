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

import org.vaadin.olli.FileDownloadWrapper;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.StreamResource;

public class DownloadDialog extends ConfirmationDialog {
	
	public Runnable onCancel = () -> {};
	
	Button downloadButton;
	
	public DownloadDialog(String title, String message, StreamResource myResource) {
		super(title, message);
		FileDownloadWrapper fileDownloader = new FileDownloadWrapper(myResource);
        
        fileDownloader.wrapComponent(downloadButton);
        
	}

	private static final long serialVersionUID = 1L;

	public VaadinIcon icon() {
		return VaadinIcon.INFO;
	}
	
	public HorizontalLayout buttons() {
		
		downloadButton = new Button("Herunterladen", VaadinIcon.DOWNLOAD.create());
		
        Button cancelButton = new Button("Schliessen", VaadinIcon.CLOSE.create());
		cancelButton.addClickListener(e2 -> {
			if (onCancel != null) onCancel.run();
			this.close(); 
		});
        
		HorizontalLayout buttons = new HorizontalLayout(downloadButton, cancelButton);
		return buttons;
	}
	
	public DownloadDialog cancel(Runnable onCancel)
	{
		this.onCancel = onCancel;
		return this;
	}

}
