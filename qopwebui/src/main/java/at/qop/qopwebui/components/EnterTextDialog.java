package at.qop.qopwebui.components;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class EnterTextDialog extends Window {
	
	private static final long serialVersionUID = 1L;
	
	private String text;

	private ValueChangeListener<String> cl;
	
	public EnterTextDialog(String title, String message)
	{
		super(title);
		this.setModal(true);
		this.text = message;

       	Window subWindow = this;
    	subWindow.setModal(true);
        VerticalLayout subContent = new VerticalLayout();
        subContent.setSpacing(true);
        subWindow.setContent(subContent);

        TextField profileName = new TextField(text);
        profileName.focus();
        
		subContent.addComponent(profileName);
        Button okButton = new Button("OK");
        okButton.setClickShortcut(KeyCode.ENTER);
		okButton.addClickListener(e2 -> {
			if (cl != null) cl.valueChange(new ValueChangeEvent<String>(profileName, null, true));
			this.close(); 
		});
        
        subContent.addComponent(okButton);
	}
	
	public EnterTextDialog ok(ValueChangeListener<String> cl)
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
