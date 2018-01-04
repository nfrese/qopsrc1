package at.qop.qopwebui.admin;

import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class ProfilesTab extends AbstractTab {

	@Override
	public Component initialize(Page page) {
    	final VerticalLayout vl = new VerticalLayout();
    	vl.setMargin(true);
		return vl;
	}

}
