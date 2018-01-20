package at.qop.qopwebui.admin;

import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import at.qop.qoplib.imports.PerformAddressUpdate;
import at.qop.qopwebui.components.ChartDialog;

public class MiscTab extends AbstractTab {

	@Override
	public Component initialize(Page page) {
		TextField tfBezirkFilter = new TextField("Bezirkfilter (zb: 01)");
		Button button = new Button("Update Addresses");
		button.addClickListener(e -> {
			//layout.addComponent(new ProgressBar());
			new Notification("Lade die Adressen herunter",
					"Moment",
					Notification.Type.HUMANIZED_MESSAGE).show(page);
			new PerformAddressUpdate().updateAddresses(tfBezirkFilter.getValue());
		});

		final VerticalLayout vl = new VerticalLayout(new HorizontalLayout(button, tfBezirkFilter),
				new BatchControl().init());
		vl.setMargin(true);
		return vl;
	}

}
