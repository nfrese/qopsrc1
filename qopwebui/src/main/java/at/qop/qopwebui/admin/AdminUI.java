/* 
 * Copyright (C) 2018 Norbert Frese
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
*/

package at.qop.qopwebui.admin;

import java.io.IOException;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.theme.Theme;

import at.qop.qopwebui.ProtectedUI;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;

@Route("/qop/ui/admin")
public class AdminUI extends ProtectedUI {
	
	private static final long serialVersionUID = 1L;
	
	public AdminUI() {
		ainit(null);
	}
	
	@Override
    protected void ainit(VaadinRequest vaadinRequest) {
		
        final VerticalLayout layout = new VerticalLayout();
       
        TabSheet tabs = new TabSheet();
       // tabs.setCaption("QOP Admin");
        tabs.setHeight(90.0f, Unit.PERCENTAGE);
        tabs.setWidth(100.0f, Unit.PERCENTAGE);
//        tabs.addStyleName(ValoTheme.TABSHEET_FRAMED);
//        tabs.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        
        {   
        	ProfilesTab tab = new ProfilesTab();
        	Component c = tab.initialize(this.getUI().get().getPage());
        	tabs.add("Profile",c);
        }
        {   
        	AnalysisTab tab = new AnalysisTab();
        	Component c = tab.initialize(this.getUI().get().getPage());
        	tabs.add("Auswertungen",c);
        }
        {   
        	AnalysisFunctionTab tab = new AnalysisFunctionTab();
        	Component c = tab.initialize(this.getUI().get().getPage());
        	tabs.add("Auswertungsfunktionen",c);
        }
        {   
        	AbstractLayerDataTab tab = new VectorLayerDataTab();
        	Component c = tab.initialize(this.getUI().get().getPage());
        	tabs.add("Vektor Daten",c);
        }
        {   
        	AbstractLayerDataTab tab = new RasterLayerDataTab();
        	Component c = tab.initialize(this.getUI().get().getPage());
        	tabs.add("Raster Daten",c);
        }        
        {
        	MiscTab tab = new MiscTab();
        	Component c = tab.initialize(this.getUI().get().getPage());
        	tabs.add("Misc", c);
        }        
        layout.add(tabs);
        layout.setSizeFull();
        add(layout);
    }
	
//    @WebServlet(urlPatterns = "/qop/ui/admin/*", name = "AdminUIServlet", asyncSupported = true)
//    //@VaadinServletConfiguration(ui = AdminUI.class, productionMode = false)
//    @WebInitParam(name = "UI", value = "at.qop.qopwebui.admin.AdminUI")
//    public static class AdminUIServlet extends VaadinServlet {
//		private static final long serialVersionUID = 1L;
//		
//    }

	@Override
	protected boolean requiresAdminRole() {
		return true;
	}
}
