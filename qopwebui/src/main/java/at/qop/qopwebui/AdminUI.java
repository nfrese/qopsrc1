package at.qop.qopwebui;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.UpdateAddresses;
import at.qop.qoplib.dbbatch.DbBatch;
import at.qop.qoplib.dbbatch.DbRecord;
import at.qop.qoplib.dbbatch.DbTable;
import at.qop.qoplib.dbbatch.DbTableReader;
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
        	StringBuilder html = new StringBuilder();
        	
   			IGenericDomain gd = LookupSessionBeans.genericDomain();

    		QopDBMetadata meta = gd.getMetadata();
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
 
            tabs.addTab(vl, "Layers");
        }
        {   
        	IGenericDomain gd = LookupSessionBeans.genericDomain();

    		QopDBMetadata meta = gd.getMetadata();
    		
			ListSelect<QopDBTable> listSelect = new ListSelect<QopDBTable>("Layer auswählen...", meta.tables);
            listSelect.setRows(6);
            listSelect.setHeight(100.0f, Unit.PERCENTAGE);
     
            Grid<DbRecord> grid = new Grid<DbRecord>();
            grid.setWidth(100.0f, Unit.PERCENTAGE);
            grid.setHeight(100.0f, Unit.PERCENTAGE);
            
            Page page = this.getPage();
			listSelect.addValueChangeListener(
					event -> { 
						new Notification("Value changed:", String.valueOf(event.getValue())).show(page); 
						if (event.getValue().size() == 1)
						{
							QopDBTable table = event.getValue().iterator().next();
							//grid.setDataProvider(dataProvider(table));
						}
					} );
        	
        	final HorizontalLayout hl = new HorizontalLayout(listSelect, grid);
        	hl.setMargin(true);
        	tabs.addTab(hl, "Layer Data");
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

	private DataProvider<DbRecord, ?> dataProvider(QopDBTable table) {
		try {
		IGenericDomain gd_ = LookupSessionBeans.genericDomain();
		DbTableReader tableReader = new DbTableReader() {

			@Override
			public void metadata(DbTable table) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void record(DbRecord record) {
				// TODO Auto-generated method stub
				
			}
			
		};
		gd_.readTable("select * from " + table.name, tableReader);
		} catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
		return null;
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
	
	
    @WebServlet(urlPatterns = "/admin/*", name = "AdminUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = AdminUI.class, productionMode = false)
    public static class AdminUIServlet extends VaadinServlet {
		private static final long serialVersionUID = 1L;
    }
}
