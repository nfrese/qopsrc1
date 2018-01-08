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
        	LayersTab tab = new LayersTab();
        	Component c = tab.initialize(this.getPage());
            tabs.addTab(c, "Layers");
        }
        {   
        	LayerDataTab tab = new LayerDataTab();
        	Component c = tab.initialize(this.getPage());
        	tabs.addTab(c, "Layer Data");
        }        
        {   
        	ProfilesTab tab = new ProfilesTab();
        	Component c = tab.initialize(this.getPage());
        	tabs.addTab(c, "Profiles");
        }
        {   
        	AnalysisTab tab = new AnalysisTab();
        	Component c = tab.initialize(this.getPage());
        	tabs.addTab(c, "Auswertungen");
        }
        {
        	MiscTab tab = new MiscTab();
        	Component c = tab.initialize(this.getPage());
        	tabs.addTab(c, "Misc");
        }        
        layout.addComponents(title, tabs);
        
        setContent(layout);
    }
	
    @WebServlet(urlPatterns = "/admin/*", name = "AdminUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = AdminUI.class, productionMode = false)
    public static class AdminUIServlet extends VaadinServlet {
		private static final long serialVersionUID = 1L;
    }
}
