package at.qop.qopwebui.admin.forms;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationException;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import at.qop.qoplib.entities.Profile;
import at.qop.qopwebui.components.JsValidator;
import at.qop.qoplib.entities.Analysis;

public class ProfileForm extends AbstractForm {
	
	private static final long serialVersionUID = 1L;
	
	private final Profile profile;
	private Binder<Profile> binder;
	
	public ProfileForm(String title, Profile profile, boolean create) {
		super(title, create);
		this.profile = profile;
		binder.readBean(profile);
	}
	
	@Override
	protected Component initComponents(boolean create) {

		VerticalLayout vl = new VerticalLayout();

		binder = new Binder<>();
		
		{
			TextField textField = new TextField("Profilename");
			textField.setEnabled(create);
			vl.addComponent(textField);
			binder.bind(textField, o -> o.name, (o,v) -> o.name = v);
		}	
		{
			TextField textField = new TextField("Beschreibung");
			vl.addComponent(textField);
			binder.bind(textField, o -> o.description, (o,v) -> o.description = v);
		}	
		{
			TextArea textArea = new TextArea("Auswertungs-Funktion (Javascript)");
			textArea.setWidth(600, Unit.PIXELS);
			textArea.setHeight(200, Unit.PIXELS);
			vl.addComponent(textArea);
			binder.forField(textArea).withValidator(new JsValidator()).bind(o -> o.aggrfn, (o,v) -> o.aggrfn = v);
		}
		return vl;
	}

	@Override
	protected void saveData() {
		try {
			binder.writeBean(profile);
		} catch (ValidationException e) {
			new Notification("Validation error count: "
					+ e.getValidationErrors().size()).show(UI.getCurrent().getPage());
		}
	}
	

}
