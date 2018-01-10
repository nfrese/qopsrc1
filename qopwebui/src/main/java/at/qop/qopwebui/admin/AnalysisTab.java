package at.qop.qopwebui.admin;

import java.util.Set;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.entities.Analysis;
import at.qop.qopwebui.admin.forms.AnalysisForm;
import at.qop.qopwebui.components.ConfirmationDialog;

public class AnalysisTab extends AbstractTab {

	@Override
	public Component initialize(Page page) {
 
        Grid<Analysis> grid = new Grid<Analysis>();
        grid.setWidth(100.0f, Unit.PERCENTAGE);
        grid.setHeight(100.0f, Unit.PERCENTAGE);

        Button addanalysisButton = new Button("Auswertung hinzufügen...");
        addanalysisButton.addClickListener(e -> {
        	
        	Analysis analysis = new Analysis();
        	
    		new AnalysisForm("Auswertung hinzufügen", analysis).ok(dummy -> {
    			LookupSessionBeans.profileDomain().createAnalysis(analysis);
    			refreshGrid(grid);
    		}) .show();

        });
        
        Button editanalysisButton = new Button("Auswertung bearbeiten...");
        editanalysisButton.setEnabled(false);
        editanalysisButton.addClickListener(e -> {
        	if (grid.getSelectedItems().size() == 1) {
        		Analysis analysis = grid.getSelectedItems().iterator().next();
        		
        		new AnalysisForm("Auswertung bearbeiten", analysis).ok(dummy -> {
        			LookupSessionBeans.profileDomain().updateAnalysis(analysis);
        			refreshGrid(grid);
        		}) .show();
        		
        	}
		} );
        
        Button deleteanalysisButton = new Button("Auswertung loeschen...");
        deleteanalysisButton.setEnabled(false);
        deleteanalysisButton.addClickListener(e -> {
        	if (grid.getSelectedItems().size() == 1) {
        		Analysis analysis = grid.getSelectedItems().iterator().next();
            		new ConfirmationDialog("Rückfrage", "Auswertung " + analysis.name + " wirklich löschen?")
            			.ok(e3 -> {
            				LookupSessionBeans.profileDomain().dropAnalysis(analysis);
            				refreshGrid(grid);
            			}).show();
            	}
        		
        	}
		);
        
		grid.addSelectionListener(event -> {
			
			Set<Analysis> selectedItems = event.getAllSelectedItems();
			editanalysisButton.setEnabled(selectedItems.size() == 1);
			deleteanalysisButton.setEnabled(selectedItems.size() == 1);
		});
		
    	final VerticalLayout vl = new VerticalLayout(grid, new HorizontalLayout(addanalysisButton, editanalysisButton, deleteanalysisButton));
    	vl.setMargin(true);
    	vl.setSizeFull();
    	
    	refreshGrid(grid);
    	
		return vl;
	}

	private void refreshGrid(Grid<Analysis> grid) {
		grid.removeAllColumns();
		grid.addColumn(item -> item.name).setCaption("Name");
		grid.addColumn(item -> item.description).setCaption("Beschreibung");
		grid.addColumn(item -> item.query).setCaption("SQL").setMaximumWidth(300);
		grid.addColumn(item -> item.geomfield).setCaption("Geometrie-Feld");
		grid.addColumn(item -> (item.mode != null ? item.mode.desc : "")).setCaption("Routing Modus");
		grid.addColumn(item -> item.evalfn).setCaption("Auswertungs-Funktion (Javascript)").setMaximumWidth(300);
		grid.addColumn(item -> item.radius).setCaption("Radius");

		DataProvider<Analysis, ?> dataProvider = new ListDataProvider<Analysis>(LookupSessionBeans.profileDomain().listAnalyses());
		grid.setDataProvider(dataProvider);
	}

}
