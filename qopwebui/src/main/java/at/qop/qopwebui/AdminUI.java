package at.qop.qopwebui;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import at.qop.qoplib.Configuration;
import at.qop.qoplib.LookupDomains;
import at.qop.qoplib.UpdateAddresses;
import at.qop.qoplib.dbbatch.DbBatch;
import at.qop.qoplib.dbmetadata.QopDBColumn;
import at.qop.qoplib.dbmetadata.QopDBMetadata;
import at.qop.qoplib.dbmetadata.QopDBTable;
import at.qop.qoplib.domains.IConfigDomain;

@Theme("mytheme")
public class AdminUI extends UI {

	private static final long serialVersionUID = 1L;

	@Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();
        //layout.setHeight(100.0f, Unit.PERCENTAGE);
        
        final Label title  = new Label("QOP Admin area");
        
        TabSheet tabs = new TabSheet();
        tabs.setHeight(100.0f, Unit.PERCENTAGE);
        tabs.setWidth(100.0f, Unit.PERCENTAGE);
        tabs.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabs.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        
        {
        	StringBuilder html = new StringBuilder();
        	
    		IConfigDomain cd;
    		try {
    			cd = LookupDomains.configDomain();
    		} catch (NamingException e) {
    			throw new RuntimeException(e);
    		}
    		QopDBMetadata meta = cd.getMetadata();
    		for (QopDBTable table : meta.tables)
    		{
    			if (table.isGeometric())
    			{
    				html.append("<p>" + table.name + "</p>");
    				for (QopDBColumn column : table.columns)
    				{
    					html.append("<ul>" + column.name + "(" + column.typename + ")</ul>");
    				}
    			}
    		}
    		
        	
			final Label label = new Label(html.toString(), ContentMode.HTML);
            label.setWidth(100.0f, Unit.PERCENTAGE);
 
            final VerticalLayout vl = new VerticalLayout(label);
            vl.setMargin(true);
 
            tabs.addTab(vl, "Data Layers");
        }
        {   
        	final VerticalLayout vl = new VerticalLayout();
        	vl.setMargin(true);

        	tabs.addTab(vl, "Profiles");
        }
        {
        	Button button = new Button("Update Addresses");
            button.addClickListener(e -> {
                //layout.addComponent(new ProgressBar());
            	new Notification("Lade die Adressen herunter",
                        "Moment",
                        Notification.Type.HUMANIZED_MESSAGE).show(getPage());
                updateAddresses();

                        
            });
        	
        	
        	final VerticalLayout vl = new VerticalLayout(button);
        	vl.setMargin(true);

        	tabs.addTab(vl, "Misc");
        }        
        layout.addComponents(title, tabs);
        
        setContent(layout);
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
		
		IConfigDomain cd;
		try {
			cd = LookupDomains.configDomain();
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
		
		try {
			cd.batchUpdate(p);
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
	
	
    @WebServlet(urlPatterns = "/admin/*", name = "AdminUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = AdminUI.class, productionMode = false)
    public static class AdminUIServlet extends VaadinServlet {
		private static final long serialVersionUID = 1L;
    }
}
