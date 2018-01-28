package at.qop.qopwebui.components;

import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class AbstractDialog extends Window {
	
	private static final long serialVersionUID = 1L;

	public AbstractDialog() {
		super();
	}

	public AbstractDialog(String caption, Component content) {
		super(caption, content);
	}

	public AbstractDialog(String caption) {
		super(caption);
	}

	public void show()
	{
		this.center();
		UI.getCurrent().addWindow(this);
	}
}
