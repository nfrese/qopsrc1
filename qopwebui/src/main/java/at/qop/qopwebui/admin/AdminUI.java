package at.qop.qopwebui.admin;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import at.qop.qoplib.ConfigFile;
import at.qop.qopwebui.components.EnterTextDialog;

@Theme("valo")
public class AdminUI extends UI {
	
	private static final long serialVersionUID = 1L;

	@Override
    protected void init(VaadinRequest vaadinRequest) {
		
		boolean authenticated = "true".equals(UI.getCurrent().getSession().getAttribute("authenticated"));
		
		if (!authenticated)
		{
			String adminPassword = ConfigFile.read().getAdminPassword();
			if (adminPassword == null)
			{
				init_(vaadinRequest);
			}
			else
			{
				new EnterTextDialog("Admin", "Passwort?").ok(v -> {
					if (v.getValue().equals(adminPassword))
					{
						UI.getCurrent().getSession().setAttribute("authenticated", "true");
						init_(vaadinRequest);
					}
					else
					{
						setContent(new Label("Falsches Passwort! Reload für neuen Versuch!"));
					}
				}).show();;
			}
		}
		else
		{
			init_(vaadinRequest);
		}
	}
	
    protected void init_(VaadinRequest vaadinRequest) {
		
        final VerticalLayout layout = new VerticalLayout();
       
        TabSheet tabs = new TabSheet();
        tabs.setCaption("QOP Admin");
        tabs.setHeight(90.0f, Unit.PERCENTAGE);
        tabs.setWidth(100.0f, Unit.PERCENTAGE);
        tabs.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabs.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        
        {   
        	ProfilesTab tab = new ProfilesTab();
        	Component c = tab.initialize(this.getPage());
        	tabs.addTab(c, "Profile");
        }
        {   
        	AnalysisTab tab = new AnalysisTab();
        	Component c = tab.initialize(this.getPage());
        	tabs.addTab(c, "Auswertungen");
        }
        {   
        	AnalysisFunctionTab tab = new AnalysisFunctionTab();
        	Component c = tab.initialize(this.getPage());
        	tabs.addTab(c, "Auswertungsfunktionen");
        }
        {   
        	LayerDataTab tab = new LayerDataTab();
        	Component c = tab.initialize(this.getPage());
        	tabs.addTab(c, "Daten");
        }                
        {
        	MiscTab tab = new MiscTab();
        	Component c = tab.initialize(this.getPage());
        	tabs.addTab(c, "Misc");
        }        
        layout.addComponents(tabs);
        layout.setSizeFull();
        setContent(layout);
    }
	
    @WebServlet(urlPatterns = "/admin/*", name = "AdminUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = AdminUI.class, productionMode = false)
    public static class AdminUIServlet extends VaadinServlet {
		private static final long serialVersionUID = 1L;
    }
}
