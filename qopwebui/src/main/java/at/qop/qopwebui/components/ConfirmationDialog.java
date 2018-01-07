package at.qop.qopwebui.components;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ConfirmationDialog extends Window {
	
	private static final long serialVersionUID = 1L;
	
	private String text;

	private ClickListener cl;
	
	public ConfirmationDialog(String title, String message)
	{
		super(title);
		this.setModal(true);
		this.text = message;
		VerticalLayout subContent = new VerticalLayout();
		this.setContent(subContent);
		
		Label profileName = new Label(text);
		
		subContent.addComponent(profileName);
		Button okButton = new Button("OK");
		okButton.addClickListener(e2 -> {
			if (cl != null) cl.buttonClick(null);
			this.close(); 
		});
		
		Button cancelButton = new Button("Abbruch");
		cancelButton.addClickListener(e2 -> {
			this.close(); 
		});

		subContent.addComponent(okButton);
	}
	
	public ConfirmationDialog ok(ClickListener cl)
	{
		this.cl = cl;
		return this;
	}
	
	public void show()
	{
		this.center();
		UI.getCurrent().addWindow(this);
	}
}
