package at.qop.qopwebui;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.annotation.WebServlet;

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
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
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
							prepareGrid(grid, table);
							
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

	private void prepareGrid(Grid<DbRecord> grid, QopDBTable table) {
		
		String baseSql = "select * from " + table.name;
		
		grid.setDataProvider(dataProvider(table, baseSql));
		grid.removeAllColumns();
		
		IGenericDomain gd_ = LookupSessionBeans.genericDomain();
		
		DbTableScanner tableReader = new DbTableScanner();
		try {
			gd_.readTable(baseSql, tableReader);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		for (int i = 0; i < tableReader.table.colNames.length; i++)
		{
			String colName = tableReader.table.colNames[i];

			
			final int i_ = i;
			grid.addColumn(item -> stringRepresentation(tableReader.table, i_, item)).setCaption(colName);
			
		}
	}

	private Object stringRepresentation(DbTable table, final int i, DbRecord item) {
		
		int coltype = table.sqlTypes[i];
		if ("geometry".equals(table.typeNames[i]))
		{
			WKBReader wkbReader = new WKBReader();  
			try {
				return wkbReader.read(WKBReader.hexToBytes(String.valueOf(item.values[i])));
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		
		
		return item.values[i];
	}

	public static interface DataService {

		int count();

		Stream<DbRecord> fetch(int offset, int limit, List<String> sortOrders);

		String createSort(String sorted, boolean b);
		
	}
	
	private final class DbTableReader extends AbstractDbTableReader {
		
		public DbTable table = null;
		public List<DbRecord> records = new ArrayList<>();
		
		@Override
		public void metadata(DbTable table) {
			this.table = table;
		}

		@Override
		public void record(DbRecord record) {
			records.add(record);
		}
	}
	
	private final class DbTableScanner extends AbstractDbTableReader {
		
		public DbTable table = null;
		
		@Override
		public void metadata(DbTable table) {
			this.table = table;
		}

		@Override
		public void record(DbRecord record) {
			cancelled = true;
		}
	}
	
	private DataProvider<DbRecord, ?> dataProvider(QopDBTable table, String baseSql) {
		try {
			
			DataService dataService = new DataService() {

				@Override
				public int count() {
					
					IGenericDomain gd_ = LookupSessionBeans.genericDomain();
					try {
						DbTableReader tableReader = new DbTableReader();
						gd_.readTable("select count(*) from " + table.name, tableReader);
						return (int)(long)tableReader.records.stream().findFirst().get().values[0];
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				public Stream<DbRecord> fetch(int offset, int limit, List<String> sortOrders) {
					String orderClause = sortOrders.size() >0 ? "order by " +  sortOrders.stream()
							 .collect(Collectors.joining(", ")) : "";
					String sql = baseSql + orderClause;
					
					IGenericDomain gd_ = LookupSessionBeans.genericDomain();
					DbTableReader tableReader = new DbTableReader();
					try {
						gd_.readTable(sql, tableReader);
						return tableReader.records.stream();
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				public String createSort(String sorted, boolean b) {
					return sorted + (b ? " desc" : "");
				}
				
			};
			
			DataProvider<DbRecord, Void> dataProvider = DataProvider.fromCallbacks(
					  query -> {
					    List<String> sortOrders = new ArrayList<>();
					    for(SortOrder<String> queryOrder : query.getSortOrders()) {
					    	String sort = dataService.createSort(
					        // The name of the sorted property
					        queryOrder.getSorted(),
					        // The sort direction for this property
					        queryOrder.getDirection() == SortDirection.DESCENDING);
					      sortOrders.add(sort);
					    }

					    return dataService.fetch(
					        query.getOffset(),
					        query.getLimit(),
					        sortOrders
					      );
					  },
					  // The number of persons is the same regardless of ordering
					  query -> dataService.count()
					);
			

		return dataProvider;
		} catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
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
