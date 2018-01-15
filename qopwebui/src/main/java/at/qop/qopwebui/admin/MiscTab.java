package at.qop.qopwebui.admin;

import java.util.List;

import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.UpdateAddresses;
import at.qop.qoplib.batch.BatchCalculation;
import at.qop.qoplib.dbconnector.DbBatch;
import at.qop.qoplib.domains.IGenericDomain;
import at.qop.qoplib.entities.Profile;

public class MiscTab extends AbstractTab {

	private Profile currentProfile;

	@Override
	public Component initialize(Page page) {
		TextField tfBezirkFilter = new TextField("Bezirkfilter (zb: 01)");
		
		Button button = new Button("Update Addresses");
        button.addClickListener(e -> {
            //layout.addComponent(new ProgressBar());
        	new Notification("Lade die Adressen herunter",
                    "Moment",
                    Notification.Type.HUMANIZED_MESSAGE).show(page);
            updateAddresses(tfBezirkFilter.getValue());
        });

        Button batchButton = new Button("Batch Calculation");
        batchButton.addClickListener(e -> {

        	if (currentProfile != null)
        	{
        		BatchCalculation bc = new BatchCalculation(currentProfile);
        		bc.run();
        	}
        });

        List<Profile> profiles = LookupSessionBeans.profileDomain().listProfiles();
		ComboBox<Profile> profileCombo = new ComboBox<>("Profilauswahl", profiles);
		if (profiles.size() > 0)
		{
			profileCombo.setSelectedItem(profiles.get(0));
			currentProfile = profiles.get(0);
		}
		profileCombo.setEmptySelectionAllowed(false);
		profileCombo.setTextInputAllowed(false);

		profileCombo.addSelectionListener(event -> {
			currentProfile = event.getSelectedItem().isPresent() ? event.getSelectedItem().get() : null;
		});
        
    	final VerticalLayout vl = new VerticalLayout(new HorizontalLayout(button, tfBezirkFilter, batchButton));
    	vl.setMargin(true);
		return vl;
	}

	private void updateAddresses(String bezFilter)
	{
		bezFilter = bezFilter == null || bezFilter.trim().isEmpty() ? null : bezFilter; 
		
		try {
			UpdateAddresses updateAddresses = new UpdateAddresses(bezFilter);
			updateAddresses.onPacket(p -> forward(p));
			updateAddresses.runUpdate();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private Void forward(DbBatch p) {
		System.out.println(p);
		
		try {
			IGenericDomain gd = LookupSessionBeans.genericDomain();
			gd.batchUpdate(p);
		} catch (Exception ex)
		{
			if (p.mayFail)
			{
				System.err.println("MAYFAIL: " + ex.getMessage());
			}
			else
			{
				throw new RuntimeException(ex);
			}
		}
		
		return null;
	}
		
	
}
