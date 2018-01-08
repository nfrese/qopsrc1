package at.qop.qopwebui.admin.forms;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationException;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileLayer;

public class LayerGroupForm extends AbstractForm {
	
	private static final long serialVersionUID = 1L;
	
	private final Profile profile;
	private final ProfileLayer profileLayer;
	private Binder<ProfileLayer> binder;
	
	public LayerGroupForm(String title, Profile profile, ProfileLayer profileLayer) {
		super(title);
		this.profile = profile;
		this.profileLayer = profileLayer;
		binder.readBean(profileLayer);
	}
	
	@Override
	protected Component initComponents() {

		VerticalLayout vl = new VerticalLayout();

		binder = new Binder<>();
		
		{
			TextField textField = new TextField("Tabellenname");
			vl.addComponent(textField);
			binder.bind(textField, o -> o.tablename, (o,v) -> o.tablename = v);
		}	
		{
			TextField textField = new TextField("Beschreibung");
			vl.addComponent(textField);
			binder.bind(textField, o -> o.description, (o,v) -> o.description = v);
		}	
		{
			TextField textField = new TextField("SQL");
			textField.setWidth(600, Unit.PIXELS);
			vl.addComponent(textField);
			binder.bind(textField, o -> o.query, (o,v) -> o.query = v);
		}	
		{
			TextField textField = new TextField("Geometrie-Feld");
			vl.addComponent(textField);
			binder.bind(textField, o -> o.geomfield, (o,v) -> o.geomfield = v);
		}	
		{
			TextArea textArea = new TextArea("Auswertungs-Funktion (Javascript)");
			textArea.setWidth(600, Unit.PIXELS);
			textArea.setHeight(200, Unit.PIXELS);
			vl.addComponent(textArea);
			binder.bind(textArea, o -> o.evalfn, (o,v) -> o.evalfn = v);
		}
		{
			TextField textField = new TextField("Radius");
			vl.addComponent(textField);
			binder.bind(textField, o -> o.radius + "", (o,v) -> o.radius = Double.parseDouble(v));
		}
		
		return vl;
	}

	@Override
	protected void saveData() {
		try {
			binder.writeBean(profileLayer);
		} catch (ValidationException e) {
			new Notification("Validation error count: "
					+ e.getValidationErrors().size()).show((Page)this.getParent());
		}
	}
	

}
