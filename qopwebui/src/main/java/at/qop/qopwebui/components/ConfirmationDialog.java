package at.qop.qopwebui.components;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ConfirmationDialog extends Window {
	
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
	
	public void show()
	{
		this.center();
		UI.getCurrent().addWindow(this);
	}
}
