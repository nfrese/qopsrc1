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
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;

public class DownloadDialog extends ConfirmationDialog {
	
	public Runnable onCancel = () -> {};
	
	Button downloadButton;
	
	public DownloadDialog(String title, String message, Resource myResource) {
		super(title, message);
		FileDownloader fileDownloader = new FileDownloader(myResource);
        
        fileDownloader.extend(downloadButton);
        
	}

	private static final long serialVersionUID = 1L;

	public VaadinIcons icon() {
		return VaadinIcons.INFO;
	}
	
	public HorizontalLayout buttons() {
		
		downloadButton = new Button("Herunterladen", VaadinIcons.DOWNLOAD);
		
        Button cancelButton = new Button("Schliessen", VaadinIcons.CLOSE);
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
