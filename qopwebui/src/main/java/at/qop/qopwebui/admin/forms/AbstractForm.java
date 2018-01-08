package at.qop.qopwebui.admin.forms;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public abstract class AbstractForm extends Window {

	private static final long serialVersionUID = 1L;
	
	private ClickListener cl;

	public AbstractForm(String title)
	{
		super(title);
		this.setModal(true);
		Component c = initComponents();
		VerticalLayout subContent = new VerticalLayout();
		subContent.addComponent(c);
		this.setContent(subContent);
		
		Button okButton = new Button("Speichern");
		okButton.addClickListener(e2 -> {
			saveData();
			if (cl != null) cl.buttonClick(null);
			this.close();
		});
		subContent.addComponent(okButton);
		
		Button cancelButton = new Button("Abbruch");
		cancelButton.addClickListener(e2 -> {
			this.close(); 
		});
		subContent.addComponent(cancelButton);
	}

	public void show()
	{
		this.center();
		UI.getCurrent().addWindow(this);
	}
	
	public AbstractForm ok(ClickListener cl)
	{
		this.cl = cl;
		return this;
	}
	
	protected abstract void saveData();

	protected abstract Component initComponents();
}
