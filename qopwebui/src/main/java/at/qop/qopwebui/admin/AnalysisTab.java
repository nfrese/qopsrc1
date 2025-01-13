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
import java.util.Set;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.Utils;
import at.qop.qoplib.entities.Analysis;
import at.qop.qopwebui.admin.forms.AnalysisForm;
import at.qop.qopwebui.components.ConfirmationDialog;

public class AnalysisTab extends AbstractTab {

	@Override
	public Component initialize(Page page) {
 
        Grid<Analysis> grid = new Grid<Analysis>();
        grid.setWidth(100.0f, Unit.PERCENTAGE);
        grid.setHeight(100.0f, Unit.PERCENTAGE);

        Button addanalysisButton = new Button("Auswertung hinzufügen...", VaadinIcon.PLUS.create());
        addanalysisButton.addClickListener(e -> {
        	
        	Analysis analysis = new Analysis();
        	
    		new AnalysisForm("Auswertung hinzufügen", analysis, true).ok(dummy -> {
    			LookupSessionBeans.profileDomain().createAnalysis(analysis);
    			refreshGrid(grid);
    		}) .show();

        });
        
        Button editanalysisButton = new Button("Auswertung bearbeiten...", VaadinIcon.EDIT.create());
        editanalysisButton.setEnabled(false);
        editanalysisButton.addClickListener(e -> {
        	if (grid.getSelectedItems().size() == 1) {
        		Analysis analysis = grid.getSelectedItems().iterator().next();
        		
        		new AnalysisForm("Auswertung bearbeiten", analysis, false).ok(dummy -> {
        			LookupSessionBeans.profileDomain().updateAnalysis(analysis);
        			refreshGrid(grid);
        		}) .show();
        		
        	}
		} );
        
        Button cloneanalysisButton = new Button("Auswertung klonen...", VaadinIcon.QUOTE_RIGHT.create());
        cloneanalysisButton.setEnabled(false);
        cloneanalysisButton.addClickListener(e -> {
        	if (grid.getSelectedItems().size() == 1) {
        		Analysis analysis = grid.getSelectedItems().iterator().next();
        		try {
					Analysis clone = Utils.deepClone(analysis);
					clone.name = clone.name + "_" + ((int)(Math.random()*100));
					
					new AnalysisForm("Auswertung klonen", clone, true).ok(dummy -> {
						LookupSessionBeans.profileDomain().createAnalysis(clone);
						refreshGrid(grid);
					}) .show();
					
				} catch (ClassNotFoundException | IOException e1) {
					throw new RuntimeException(e1);
				}
        		
        		
        	}
		} );
        
        Button deleteanalysisButton = new Button("Auswertung loeschen...", VaadinIcon.TRASH.create());
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
			cloneanalysisButton.setEnabled(selectedItems.size() == 1);
			deleteanalysisButton.setEnabled(selectedItems.size() == 1);
		});
		
    	final VerticalLayout vl = new VerticalLayout(grid, new HorizontalLayout(addanalysisButton, editanalysisButton, cloneanalysisButton, deleteanalysisButton));
    	//vl.setExpandRatio(grid, 3.0f);
    	vl.setMargin(true);
    	vl.setSizeFull();
    	
    	refreshGrid(grid);
    	
		return vl;
	}

	private void refreshGrid(Grid<Analysis> grid) {
		grid.removeAllColumns();
		grid.addColumn(item -> item.checkValid()).setHeader("Status");
		grid.addColumn(item -> item.name).setHeader("Name");
		grid.addColumn(item -> item.description).setHeader("Beschreibung").setWidth("300px");
		grid.addColumn(item -> item.query).setHeader("SQL").setWidth("300px");
		grid.addColumn(item -> item.geomfield).setHeader("Geometrie-Feld");
		grid.addColumn(item -> (item.mode != null ? item.mode.desc : "")).setHeader("Routing Modus");
		grid.addColumn(item -> item.analysisfunction).setHeader("Auswertungs-Funktion").setWidth("300px");
		grid.addColumn(item -> item.ratingfunc).setHeader("Rating-Funktion (Javascript)").setWidth("300px");
		grid.addColumn(item -> item.radius).setHeader("Radius");

		DataProvider<Analysis, ?> dataProvider = new ListDataProvider<Analysis>(LookupSessionBeans.profileDomain().listAnalyses());
		grid.setDataProvider(dataProvider);
	}

}
