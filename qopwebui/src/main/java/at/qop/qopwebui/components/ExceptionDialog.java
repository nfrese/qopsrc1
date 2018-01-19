package at.qop.qopwebui.components;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ExceptionDialog extends Window {
	
	private static final long serialVersionUID = 1L;
	
	private String text;

	private ClickListener cl;
	
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
		Button okButton = new Button("OK", VaadinIcons.CHECK);
		okButton.addClickListener(e2 -> {
			if (cl != null) cl.buttonClick(null);
			this.close(); 
		});
		Button cancelButton = new Button("Abbruch", VaadinIcons.CLOSE);
		cancelButton.addClickListener(e2 -> {
			this.close(); 
		});
		subContent.addComponent(new HorizontalLayout(okButton, cancelButton));

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

	public ExceptionDialog ok(ClickListener cl)
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
