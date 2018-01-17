package at.qop.qopwebui.admin;

import java.io.IOException;
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
import at.qop.qoplib.Utils;
import at.qop.qoplib.entities.AnalysisFunction;
import at.qop.qopwebui.admin.forms.AnalysisFunctionForm;
import at.qop.qopwebui.components.ConfirmationDialog;

public class AnalysisFunctionTab extends AbstractTab {

	@Override
	public Component initialize(Page page) {
 
        Grid<AnalysisFunction> grid = new Grid<AnalysisFunction>();
        grid.setWidth(100.0f, Unit.PERCENTAGE);
        grid.setHeight(100.0f, Unit.PERCENTAGE);

        Button addanalysisButton = new Button("Auswertungs-Funktion hinzufügen...");
        addanalysisButton.addClickListener(e -> {
        	
        	AnalysisFunction analysis = new AnalysisFunction();
        	
    		new AnalysisFunctionForm("Auswertungs-Funktion hinzufügen", analysis, true).ok(dummy -> {
    			LookupSessionBeans.profileDomain().createAnalysisFunction(analysis);
    			refreshGrid(grid);
    		}) .show();

        });
        
        Button cloneButton = new Button("Auswertungs-Funktion klonen...");
        cloneButton.setEnabled(false);
        cloneButton.addClickListener(e -> {
        	if (grid.getSelectedItems().size() == 1) {
        		AnalysisFunction analysis = grid.getSelectedItems().iterator().next();

        		try {
        			AnalysisFunction clone = Utils.deepClone(analysis);
        			clone.name = clone.name + "_" + ((int)(Math.random()*100));

        			new AnalysisFunctionForm("Auswertungs-Funktion klonen...", clone, true).ok(dummy -> {
        				LookupSessionBeans.profileDomain().createAnalysisFunction(clone);
        				refreshGrid(grid);
        			}) .show();
        		} catch (ClassNotFoundException | IOException e1) {
        			throw new RuntimeException(e1);
        		}

        	}
        } );
        
        Button editanalysisButton = new Button("Auswertungs-Funktion bearbeiten...");
        editanalysisButton.setEnabled(false);
        editanalysisButton.addClickListener(e -> {
        	if (grid.getSelectedItems().size() == 1) {
        		AnalysisFunction analysis = grid.getSelectedItems().iterator().next();
        		
        		new AnalysisFunctionForm("Auswertungs-Funktion bearbeiten", analysis, false).ok(dummy -> {
        			LookupSessionBeans.profileDomain().updateAnalysisFunction(analysis);
        			refreshGrid(grid);
        		}) .show();
        		
        	}
		} );
        
        Button deleteanalysisButton = new Button("Auswertungs-Funktion loeschen...");
        deleteanalysisButton.setEnabled(false);
        deleteanalysisButton.addClickListener(e -> {
        	if (grid.getSelectedItems().size() == 1) {
        		AnalysisFunction analysis = grid.getSelectedItems().iterator().next();
            		new ConfirmationDialog("Rückfrage", "Auswertung " + analysis.name + " wirklich löschen?")
            			.ok(e3 -> {
            				LookupSessionBeans.profileDomain().dropAnalysisFunction(analysis);
            				refreshGrid(grid);
            			}).show();
            	}
        		
        	}
		);
        
		grid.addSelectionListener(event -> {
			
			Set<AnalysisFunction> selectedItems = event.getAllSelectedItems();
			editanalysisButton.setEnabled(selectedItems.size() == 1);
			cloneButton.setEnabled(selectedItems.size() == 1);
			deleteanalysisButton.setEnabled(selectedItems.size() == 1);
		});
		
    	final VerticalLayout vl = new VerticalLayout(grid, new HorizontalLayout(addanalysisButton,  editanalysisButton, cloneButton, deleteanalysisButton));
    	vl.setExpandRatio(grid, 3.0f);
    	vl.setMargin(true);
    	vl.setSizeFull();
    	
    	refreshGrid(grid);
    	
		return vl;
	}

	private void refreshGrid(Grid<AnalysisFunction> grid) {
		grid.removeAllColumns();
		grid.addColumn(item -> item.name).setCaption("Name");
		grid.addColumn(item -> item.description).setCaption("Beschreibung");
		grid.addColumn(item -> item.func).setCaption("Bewertungs-Funktion (Javascript)").setMaximumWidth(300);
		grid.addColumn(item -> item.rvalUnit).setCaption("Einheit Resultat");

		DataProvider<AnalysisFunction, ?> dataProvider = new ListDataProvider<AnalysisFunction>(LookupSessionBeans.profileDomain().listAnalysisFunctions());
		grid.setDataProvider(dataProvider);
	}

}
