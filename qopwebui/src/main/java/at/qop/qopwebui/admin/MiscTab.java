package at.qop.qopwebui.admin;

import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.UpdateAddresses;
import at.qop.qoplib.dbconnector.DbBatch;
import at.qop.qoplib.domains.IGenericDomain;

public class MiscTab extends AbstractTab {

	@Override
	public Component initialize(Page page) {
		Button button = new Button("Update Addresses");
        button.addClickListener(e -> {
            //layout.addComponent(new ProgressBar());
        	new Notification("Lade die Adressen herunter",
                    "Moment",
                    Notification.Type.HUMANIZED_MESSAGE).show(page);
            updateAddresses();
        });
    	
    	final VerticalLayout vl = new VerticalLayout(button);
    	vl.setMargin(true);
		return vl;
	}

	private void updateAddresses()
	{
		try {
			UpdateAddresses updateAddresses = new UpdateAddresses();
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
