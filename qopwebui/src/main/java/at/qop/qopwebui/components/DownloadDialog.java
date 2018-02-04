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
