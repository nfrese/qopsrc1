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

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import at.qop.qopwebui.ProtectedUI;

@Theme("valo")
public class AdminUI extends ProtectedUI {
	
	private static final long serialVersionUID = 1L;
	
	@Override
    protected void ainit(VaadinRequest vaadinRequest) {
		
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
        	AbstractLayerDataTab tab = new VectorLayerDataTab();
        	Component c = tab.initialize(this.getPage());
        	tabs.addTab(c, "Vektor Daten");
        }
        {   
        	AbstractLayerDataTab tab = new RasterLayerDataTab();
        	Component c = tab.initialize(this.getPage());
        	tabs.addTab(c, "Raster Daten");
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

	@Override
	protected boolean requiresAdminRole() {
		return true;
	}
}
