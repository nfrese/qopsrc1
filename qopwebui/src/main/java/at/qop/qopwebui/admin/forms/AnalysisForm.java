package at.qop.qopwebui.admin.forms;

import java.util.Arrays;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationException;
import com.vaadin.server.Page;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import at.qop.qoplib.entities.Analysis;
import at.qop.qoplib.entities.ModeEnum;
import at.qop.qoplib.entities.Profile;

public class AnalysisForm extends AbstractForm {
	
	private static final long serialVersionUID = 1L;
	
	private final Analysis analysis;
	private Binder<Analysis> binder;
	
	public AnalysisForm(String title, Analysis analysis) {
		super(title);
		this.analysis = analysis;
		binder.readBean(analysis);
	}
	
	@Override
	protected Component initComponents() {

		VerticalLayout vl = new VerticalLayout();

		binder = new Binder<>();
		
		{
			TextField textField = new TextField("Name");
			textField.setWidth(450, Unit.PIXELS);
			vl.addComponent(textField);
			binder.bind(textField, o -> o.name, (o,v) -> o.name = v);
		}	
		{
			TextField textField = new TextField("Beschreibung");
			textField.setWidth(600, Unit.PIXELS);
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
			ComboBox<ModeEnum> modeCombo = new ComboBox<>("Routing Modus", Arrays.asList(ModeEnum.values()));
			modeCombo.setTextInputAllowed(false);
			vl.addComponent(modeCombo);
			binder.bind(modeCombo, o -> o.mode, (o,v) -> o.mode = v);
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
			binder.writeBean(analysis);
		} catch (ValidationException e) {
			new Notification("Validation error count: "
					+ e.getValidationErrors().size()).show((Page)this.getParent());
		}
	}
	

}
