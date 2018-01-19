package at.qop.qopwebui.components;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class InfoDialog extends ConfirmationDialog {
	
	public InfoDialog(String title, String message) {
		super(title, message);
	}

	private static final long serialVersionUID = 1L;

	public VaadinIcons icon() {
		return VaadinIcons.INFO;
	}
	
	public HorizontalLayout buttons() {
		Button okButton = new Button("OK", VaadinIcons.CHECK);
		okButton.addClickListener(e2 -> {
			if (cl != null) cl.buttonClick(null);
			this.close(); 
		});
		HorizontalLayout buttons = new HorizontalLayout(okButton);
		return buttons;
	}

}
