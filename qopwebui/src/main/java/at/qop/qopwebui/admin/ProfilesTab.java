package at.qop.qopwebui.admin;

import java.util.Collections;
import java.util.Set;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileLayer;
import at.qop.qopwebui.admin.forms.LayerProfileForm;
import at.qop.qopwebui.admin.forms.ProfileForm;
import at.qop.qopwebui.components.ConfirmationDialog;

public class ProfilesTab extends AbstractTab {

	private Profile currentProfile;
	
	@Override
	public Component initialize(Page page) {
		
		ListSelect<Profile> listSelect = new ListSelect<Profile>("Profil auswählen...");
        listSelect.setRows(4);
        listSelect.setHeight(100.0f, Unit.PERCENTAGE);
        listSelect.setWidth(170f, Unit.PIXELS);
        refreshProfileList(listSelect);
        
        Button addProfileButton = new Button("Profil hinzufügen...");
        addProfileButton.addClickListener(e -> {

        	Profile profile = new Profile();
        	
        	new ProfileForm("Neues Profil", profile).ok(e3 -> {
    			LookupSessionBeans.profileDomain().createProfile(profile);
    			refreshProfileList(listSelect);
        	}).show();

        });
        
        Button editProfileButton = new Button("Profil bearbeiten...");
        editProfileButton.setEnabled(false);
        editProfileButton.addClickListener(e -> {

        	Set<Profile> sel = listSelect.getSelectedItems();
        	if (sel.size() == 1)
        	{
        		Profile profile = sel.iterator().next();

        		new ProfileForm("Profil bearbeiten", profile).ok(e3 -> {
        			LookupSessionBeans.profileDomain().updateProfile(profile);
        			refreshProfileList(listSelect);
        		}).show();
        	}

        });
        
        Button removeProfileButton = new Button("Profil löschen...");
        removeProfileButton.setEnabled(false);
        removeProfileButton.addClickListener(e -> {
        	Set<Profile> sel = listSelect.getSelectedItems();
        	if (sel.size() == 1)
        	{
        		Profile profile = sel.iterator().next();
        		new ConfirmationDialog("Rückfrage", "Profil " + profile + " wirklich löschen?")
        			.ok(e3 -> {
        				LookupSessionBeans.profileDomain().dropProfile(profile);
        				refreshProfileList(listSelect);	
        			}).show();
        	}
        });
 
        final HorizontalLayout hl = new HorizontalLayout(listSelect, addProfileButton, editProfileButton, removeProfileButton);
        hl.setHeight(100.0f, Unit.PERCENTAGE);
        Grid<ProfileLayer> grid = new Grid<ProfileLayer>();
        grid.setWidth(100.0f, Unit.PERCENTAGE);
        grid.setHeight(100.0f, Unit.PERCENTAGE);

        Button addProfileLayerButton = new Button("Layer hinzufügen...");
        addProfileLayerButton.setEnabled(false);
        addProfileLayerButton.addClickListener(e -> {
        	
        	ProfileLayer profileLayer = new ProfileLayer();
			//profileLayer.profile = currentProfile;
        	
    		new LayerProfileForm("Profile-Layer Bearbeiten", currentProfile, profileLayer).ok(dummy -> {
    			currentProfile.profileLayer.add(profileLayer);
    			LookupSessionBeans.profileDomain().updateProfile(currentProfile);
    			refreshGrid(grid);
    		}) .show();

        });
        
        Button editProfileLayerButton = new Button("Layer bearbeiten...");
        editProfileLayerButton.setEnabled(false);
        editProfileLayerButton.addClickListener(e -> {
        	if (grid.getSelectedItems().size() == 1) {
        		ProfileLayer profileLayer = grid.getSelectedItems().iterator().next();
        		
        		new LayerProfileForm("Profile-Layer Bearbeiten", currentProfile, profileLayer).ok(dummy -> {
        			LookupSessionBeans.profileDomain().updateProfile(currentProfile);
        			refreshGrid(grid);
        		}) .show();
        		
        	}
		} );
        
        Button deleteProfileLayerButton = new Button("Layer loeschen...");
        deleteProfileLayerButton.setEnabled(false);
        deleteProfileLayerButton.addClickListener(e -> {
        	if (grid.getSelectedItems().size() == 1) {
        		ProfileLayer profileLayer = grid.getSelectedItems().iterator().next();
            		new ConfirmationDialog("Rückfrage", "Layer " + profileLayer.tablename + " wirklich löschen?")
            			.ok(e3 -> {
            				currentProfile.profileLayer.remove(profileLayer);
            				LookupSessionBeans.profileDomain().updateProfile(currentProfile);
            				refreshGrid(grid);
            			}).show();
            	}
        		
        	}
		);
        
		listSelect.addValueChangeListener(
				event -> { 
					removeProfileButton.setEnabled(event.getValue().size() == 1);
					editProfileButton.setEnabled(event.getValue().size() == 1);
					addProfileLayerButton.setEnabled(event.getValue().size() == 1);
					
					if (event.getValue().size() == 1)
					{
						Profile profile = event.getValue().iterator().next();
						currentProfile = profile;
						
					}
					else
					{
						currentProfile = null;
					}
					refreshGrid(grid);
				} );
		
		grid.addSelectionListener(event -> {
			
			Set<ProfileLayer> selectedItems = event.getAllSelectedItems();
			editProfileLayerButton.setEnabled(selectedItems.size() == 1);
			deleteProfileLayerButton.setEnabled(selectedItems.size() == 1);
		});
		
    	final VerticalLayout vl = new VerticalLayout(hl, grid, new HorizontalLayout(addProfileLayerButton, editProfileLayerButton, deleteProfileLayerButton));
    	vl.setMargin(true);
		return vl;
	}

	private void refreshProfileList(ListSelect<Profile> listSelect) {
		listSelect.setItems(LookupSessionBeans.profileDomain().listProfiles());
	}

	private void refreshGrid(Grid<ProfileLayer> grid) {
		grid.removeAllColumns();
		if (currentProfile != null)
		{
			grid.addColumn(item -> item.tablename).setCaption("Tabellenname");
			grid.addColumn(item -> item.description).setCaption("Beschreibung");
			grid.addColumn(item -> item.query).setCaption("SQL");
			grid.addColumn(item -> item.geomfield).setCaption("Geometrie-Feld");
			grid.addColumn(item -> item.evalfn).setCaption("Auswertungs-Funktion (Javascript)");
			grid.addColumn(item -> item.radius).setCaption("Radius");

			DataProvider<ProfileLayer, ?> dataProvider = new ListDataProvider<ProfileLayer>(currentProfile.profileLayer);
			grid.setDataProvider(dataProvider);
		}
		else
		{
			grid.setDataProvider(new ListDataProvider<ProfileLayer>(Collections.emptyList()));
		}
	}

}
