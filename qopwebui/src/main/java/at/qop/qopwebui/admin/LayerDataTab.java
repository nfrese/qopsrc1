package at.qop.qopwebui.admin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.vaadin.addon.leaflet.LFeatureGroup;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LTileLayer;
import org.vaadin.addon.leaflet.LeafletLayer;
import org.vaadin.addon.leaflet.util.JTSUtil;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.dbconnector.DbTableReader;
import at.qop.qoplib.dbconnector.DbTableScanner;
import at.qop.qoplib.dbconnector.metadata.QopDBMetadata;
import at.qop.qoplib.dbconnector.metadata.QopDBTable;
import at.qop.qoplib.domains.IGenericDomain;

public class LayerDataTab extends AbstractTab {

	DbTable currentTable;	
	
	@Override
	public Component initialize(Page page) {
    	IGenericDomain gd = LookupSessionBeans.genericDomain();

		QopDBMetadata meta = gd.getMetadata();
		
		ListSelect<QopDBTable> listSelect = new ListSelect<QopDBTable>("Tabelle auswählen...", meta.tables.stream().filter(t->t.isGeometric()).collect(Collectors.toList()));
        listSelect.setRows(6);
        listSelect.setHeight(100.0f, Unit.PERCENTAGE);
 
        Grid<DbRecord> grid = new Grid<DbRecord>();
        grid.setWidth(100.0f, Unit.PERCENTAGE);
        grid.setHeight(100.0f, Unit.PERCENTAGE);
        
		listSelect.addValueChangeListener(
				event -> { 
					new Notification("Value changed:", String.valueOf(event.getValue())).show(page); 
					if (event.getValue().size() == 1)
					{
						QopDBTable table = event.getValue().iterator().next();
						prepareGrid(grid, table);
						
					}
				} );
    	
		LMap leafletMap = new LMap();
		leafletMap.setWidth("600px");
		leafletMap.setHeight("400px");
		LTileLayer baseLayerOsm = new LTileLayer();
		baseLayerOsm.setUrl("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");
		leafletMap.addBaseLayer(baseLayerOsm, "OSM");
		
		LFeatureGroup lfg = new LFeatureGroup();
		leafletMap.addLayer(lfg);
		
		grid.addSelectionListener(event -> {
			
			lfg.removeAllComponents();
			
			Set<DbRecord> selectedItems = event.getAllSelectedItems();
			for (DbRecord selectedItem : selectedItems){
				
				

				Collection<Geometry> geoms = selectedItem.getGeometries(currentTable);
				for (Geometry geom : geoms)
				{
					Collection<LeafletLayer> lPoly = JTSUtil.toLayers(geom);
					lfg.addComponent(lPoly);
				}
			}
			leafletMap.zoomToContent();
		});
		leafletMap.setSizeFull();
		grid.setSizeFull();
    	final HorizontalLayout hl = new HorizontalLayout(listSelect, grid, leafletMap);
    	hl.setExpandRatio(grid, 3.0f);
    	hl.setExpandRatio(leafletMap, 1.5f);
    	hl.setSizeFull();
    	hl.setMargin(true);
    	return hl;
	}
	
	public static interface DataService {

		int count();

		Stream<DbRecord> fetch(int offset, int limit, List<String> sortOrders);

		String createSort(String sorted, boolean b);
		
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

		currentTable = tableReader.table; 
		
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

}
