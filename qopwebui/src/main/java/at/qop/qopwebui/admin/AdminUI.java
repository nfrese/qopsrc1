package at.qop.qopwebui.admin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.annotation.WebServlet;

import org.vaadin.addon.leaflet.LFeatureGroup;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LTileLayer;
import org.vaadin.addon.leaflet.LeafletLayer;
import org.vaadin.addon.leaflet.util.JTSUtil;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.UpdateAddresses;
import at.qop.qoplib.dbbatch.DbBatch;
import at.qop.qoplib.dbbatch.DbRecord;
import at.qop.qoplib.dbbatch.DbTable;
import at.qop.qoplib.dbbatch.AbstractDbTableReader;
import at.qop.qoplib.dbmetadata.QopDBColumn;
import at.qop.qoplib.dbmetadata.QopDBMetadata;
import at.qop.qoplib.dbmetadata.QopDBTable;
import at.qop.qoplib.domains.IGenericDomain;

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
