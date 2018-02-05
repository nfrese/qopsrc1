package at.qop.qopwebui.admin;

import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import at.qop.qoplib.Constants;
import at.qop.qoplib.imports.PerformAddressUpdate;
import at.qop.qopwebui.admin.forms.exports.DumpDatabase;
import at.qop.qopwebui.admin.imports.ImportShapefilesComponent;

public class MiscTab extends AbstractTab {

	@Override
	public Component initialize(Page page) {
		
		final VerticalLayout vl = new VerticalLayout();
		
		{
			Panel p = new Panel();
			TextField tfBezirkFilter = new TextField("Bezirkfilter (zb: 01)");
			Button button = new Button("Adressen aktualisieren");
			button.addClickListener(e -> {
				new Notification("Lade die Adressen herunter",
						"Moment",
						Notification.Type.HUMANIZED_MESSAGE).show(page);
				new PerformAddressUpdate().updateAddresses(tfBezirkFilter.getValue());
			});
			HorizontalLayout hl = new HorizontalLayout(button, tfBezirkFilter);
			hl.setMargin(true);
			p.setContent(hl);
			vl.addComponent(p);
		}
		{
			Panel p = new Panel();
			p.setContent(new BatchControl().init());
			vl.addComponent(p);
		}
		{
			Panel p = new Panel();
			Button dumpConfigButton = new Button("Konfigurationstabellen sichern");
			dumpConfigButton.addClickListener(e -> {
				DumpDatabase dd = new DumpDatabase(Constants.CONFIG_TABLES);
				dd.run();
			});

			Button dumpAllButton = new Button("Komplette Datenbank sichern");
			dumpAllButton.addClickListener(e -> {
				DumpDatabase dd = new DumpDatabase();
				dd.run();
			});
			
			HorizontalLayout hl = new HorizontalLayout(dumpConfigButton, dumpAllButton);
			hl.setMargin(true);
			p.setContent(hl);
			vl.addComponent(p);
		}
		vl.setMargin(true);
		return vl;
	}

}
