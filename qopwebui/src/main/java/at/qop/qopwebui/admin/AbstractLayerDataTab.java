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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.vaadin.addon.leaflet.LFeatureGroup;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LTileLayer;
import org.vaadin.addon.leaflet.LeafletLayer;
import org.vaadin.addon.leaflet.util.JTSUtil;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.batch.PerformDelete;
import at.qop.qoplib.dbconnector.DBSingleResultTableReader;
import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.dbconnector.DbTableReader;
import at.qop.qoplib.dbconnector.DbTableScanner;
import at.qop.qoplib.dbconnector.metadata.QopDBMetadata;
import at.qop.qoplib.dbconnector.metadata.QopDBTable;
import at.qop.qoplib.domains.IGenericDomain;
import at.qop.qopwebui.admin.forms.exports.ExportShapefiles;
import at.qop.qopwebui.admin.imports.ImportFilesComponent;
import at.qop.qopwebui.admin.imports.shape.ImportShapefilesComponent;
import at.qop.qopwebui.components.ConfirmationDialog;

public abstract class AbstractLayerDataTab extends AbstractTab {

	protected DbTable currentTable;
	protected int currentLines;	
	
	protected Label tableLines = null;
	
	
	protected abstract ImportFilesComponent importFilesComponent();

	protected abstract void refreshList(IGenericDomain gd, ListSelect<QopDBTable> listSelect);

	protected abstract String baseSql(QopDBTable table);

	protected abstract String countSQL(QopDBTable table);
	
	protected abstract Object stringRepresentation(DbTable table, final int i, DbRecord item);	
	
	@Override
	public Component initialize(Page page) {
    	IGenericDomain gd = LookupSessionBeans.genericDomain();

		
		ListSelect<QopDBTable> listSelect = new ListSelect<QopDBTable>("Tabelle auswählen...");
		
		refreshList(gd, listSelect);
		
        listSelect.setRows(6);
        listSelect.setHeight(100.0f, Unit.PERCENTAGE);
 
        Grid<DbRecord> grid = new Grid<DbRecord>();
        grid.setWidth(100.0f, Unit.PERCENTAGE);
        grid.setHeight(100.0f, Unit.PERCENTAGE);
        
		listSelect.addValueChangeListener(
				event -> { 
					if (event.getValue().size() == 1)
					{
						QopDBTable table = event.getValue().iterator().next();
						prepareGrid(grid, table);
						
					}
					else
					{
						clearGrid(grid);
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
    	
		ImportFilesComponent importComponent = importFilesComponent();
		importComponent.init();

    	Button deleteLayerButton = new Button("Tabellen loeschen...", VaadinIcons.TRASH);
        deleteLayerButton.setEnabled(false);
        deleteLayerButton.addClickListener(e -> {
        	if (listSelect.getSelectedItems().size() > 0) {
        		new ConfirmationDialog("Tabellen löeschen", listSelect.getSelectedItems() + " wirklich loeschen?")
        		.ok(
        			(evt) -> { 
        				listSelect.getSelectedItems().forEach( item -> {
        					new PerformDelete(item.name);
        				});
        				refreshList(gd, listSelect);
        			}  
        		).show();
        	}
        });
        
    	Button exportLayerButton = new Button("Tabellen exportieren...", VaadinIcons.DOWNLOAD);
    	exportLayerButton.setEnabled(false);
    	exportLayerButton.addClickListener(e -> {
        	if (listSelect.getSelectedItems().size() > 0) {
        		new ConfirmationDialog("Tabellen exportieren",listSelect.getSelectedItems() + " wirklich exportieren?")
        		.ok(
        			(evt) -> { 
        				ExportShapefiles expSh = new ExportShapefiles(listSelect.getSelectedItems().stream().map(item -> item.name).collect(Collectors.toList()));
        				expSh.run();
        			}  
        		).show();
        	}
        });
        
        tableLines = new Label("");
        
    	Button showAllGeomsButton = new Button("Alle in der Karte zeigen...", VaadinIcons.PIN);
    	showAllGeomsButton.setEnabled(false);
    	showAllGeomsButton.addClickListener(e -> {

    		lfg.removeAllComponents();

    		Optional<QopDBTable> table = listSelect.getSelectedItems().stream().findFirst();
    		if (table.isPresent() && currentTable != null)
    		{

    			String baseSql = baseSql(table.get());
    			String sql = baseSql;

    			IGenericDomain gd_ = LookupSessionBeans.genericDomain();
    			DbTableReader tableReader = new DbTableReader();
    			try {
    				gd_.readTable(sql, tableReader);
    			} catch (SQLException e1) {
    				throw new RuntimeException(e1);
    			}

    			for (DbRecord selectedItem : tableReader.records){

    				Collection<Geometry> geoms = selectedItem.getGeometries(currentTable);
    				for (Geometry geom : geoms)
    				{
    					Collection<LeafletLayer> lPoly = JTSUtil.toLayers(geom);
    					lfg.addComponent(lPoly);
    				}
    			}
    		}
    		leafletMap.zoomToContent();
    	});
        
        final HorizontalLayout hlButtons = new HorizontalLayout();
        hlButtons.addComponent(importComponent);
        hlButtons.addComponent(deleteLayerButton);
        hlButtons.addComponent(exportLayerButton);

        hlButtons.addComponent(tableLines);
        hlButtons.addComponent(showAllGeomsButton);

    	
    	VerticalLayout vl = new VerticalLayout(hl, hlButtons); 
    	vl.setSizeFull();
    	vl.setExpandRatio(hl, 5.0f);
    	
    	listSelect.addValueChangeListener(
    			event -> { 
    				deleteLayerButton.setEnabled(event.getValue().size() > 0);
    				exportLayerButton.setEnabled(event.getValue().size() > 0);
    				showAllGeomsButton.setEnabled(event.getValue().size() > 0);
    			} );

    	return vl;
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
						DBSingleResultTableReader tableReader = new DBSingleResultTableReader();
						gd_.readTable(countSQL(table), tableReader);
						int lines = (int)tableReader.longResult();
						currentLines = lines;
						tableLines.setValue("Records: " + currentLines);
						return lines;
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				public Stream<DbRecord> fetch(int offset, int limit, List<String> sortOrders) {
					String orderClause = sortOrders.size() >0 ? "order by " +  sortOrders.stream()
							 .collect(Collectors.joining(", ")) : "";
					String sql = baseSql + orderClause;
					if (offset != 0) sql += " OFFSET " + offset;
					if (limit != 0) sql += " LIMIT " + limit;
					
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

					    if(query.getLimit() > 1000) throw new RuntimeException("query.getLimit() > 1000");
					    
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
	
	private void clearGrid(Grid<DbRecord> grid) {
		grid.removeAllColumns();
		DataProvider<DbRecord, ?> emptyDataProvider = new ListDataProvider<DbRecord>(Collections.emptyList());
		grid.setDataProvider(emptyDataProvider);
	}
	
	private void prepareGrid(Grid<DbRecord> grid, QopDBTable table) {
		
		String baseSql = baseSql(table);
		
		grid.removeAllColumns();
		grid.setDataProvider(dataProvider(table, baseSql));
		
		IGenericDomain gd_ = LookupSessionBeans.genericDomain();
		String sql = baseSql+ " LIMIT 1";
		
		DbTableScanner tableReader = new DbTableScanner();
		try {
			gd_.readTable(sql, tableReader);
		} catch (SQLException e) {
			throw new RuntimeException("problem executing sql " + sql, e);
		}

		currentTable = tableReader.table; 
		
		for (int i = 0; i < tableReader.table.colNames.length; i++)
		{
			String colName = tableReader.table.colNames[i];

			
			final int i_ = i;
			grid.addColumn(item -> stringRepresentation(tableReader.table, i_, item)).setCaption(colName);
			
		}
	}

}
