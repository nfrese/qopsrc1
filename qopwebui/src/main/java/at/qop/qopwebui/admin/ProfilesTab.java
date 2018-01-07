package at.qop.qopwebui.admin;

import java.util.List;
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
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileLayer;
import at.qop.qopwebui.components.AddWithNameDialog;
import at.qop.qopwebui.components.ConfirmationDialog;

public class ProfilesTab extends AbstractTab {

	Profile currentProfile;
	
	@Override
	public Component initialize(Page page) {
		
		List<Profile> profiles = LookupSessionBeans.profileDomain().listProfiles();
		
		ListSelect<Profile> listSelect = new ListSelect<Profile>("Profil auswählen...", profiles);
        listSelect.setRows(4);
        listSelect.setHeight(100.0f, Unit.PERCENTAGE);
        listSelect.setWidth(170f, Unit.PIXELS);
        
        Button addProfileButton = new Button("Profil hinzufügen...");
        addProfileButton.addClickListener(e -> {

        	new AddWithNameDialog("Neues Profil", "Profilnamen eingeben")
			.ok(e3 -> {
				Profile profile = new Profile();
            	profile.name = e3.getValue(); 
    			LookupSessionBeans.profileDomain().createProfile(profile);
    			listSelect.setItems(LookupSessionBeans.profileDomain().listProfiles());
			}).show();

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
        				listSelect.setItems(LookupSessionBeans.profileDomain().listProfiles());	
        			}).show();
        	}
        });
 
        final HorizontalLayout hl = new HorizontalLayout(listSelect, addProfileButton, removeProfileButton);
        hl.setHeight(100.0f, Unit.PERCENTAGE);
        Grid<ProfileLayer> grid = new Grid<ProfileLayer>();
        grid.setWidth(100.0f, Unit.PERCENTAGE);
        grid.setHeight(100.0f, Unit.PERCENTAGE);

        Button addProfileLayerButton = new Button("Layer hinzufügen...");
        addProfileLayerButton.setEnabled(false);
        addProfileLayerButton.addClickListener(e -> {

        	new AddWithNameDialog("Neuer Layer", "Tabellennamen eingeben")
        	.ok(e3 -> {
        		if (currentProfile != null)
        		{
        			ProfileLayer profileLayer = new ProfileLayer();
        			profileLayer.tablename = e3.getValue();
        			profileLayer.profile = currentProfile;

        			currentProfile.profileLayer.add(profileLayer);
        			//LookupSessionBeans.profileDomain().createProfileLayer(profileLayer);
        			LookupSessionBeans.profileDomain().createProfile(currentProfile);
        			refreshGrid(grid, currentProfile);
        		}

        	}).show();

        });
        
		listSelect.addValueChangeListener(
				event -> { 
					removeProfileButton.setEnabled(event.getValue().size() == 1);
					addProfileLayerButton.setEnabled(event.getValue().size() == 1);
					
					if (event.getValue().size() == 1)
					{
						Profile profile = event.getValue().iterator().next();
						currentProfile = profile;
						
						refreshGrid(grid, profile);
					}
				} );
		
    	final VerticalLayout vl = new VerticalLayout(hl, grid, addProfileLayerButton);
    	vl.setMargin(true);
		return vl;
	}

	private void refreshGrid(Grid<ProfileLayer> grid, Profile profile) {
		grid.removeAllColumns();
		
		grid.addColumn(item -> item.description).setCaption("Beschreibung").setEditorComponent(new TextField(), (item, vvalue) -> item.description = vvalue);
		grid.addColumn(item -> item.tablename).setCaption("Tabellenname").setEditorComponent(new TextField(), (item, vvalue) -> item.tablename = vvalue);
		grid.addColumn(item -> item.query).setCaption("SQL").setEditorComponent(new TextField(), (item, vvalue) -> item.query = vvalue);
		grid.addColumn(item -> item.geomfield).setCaption("Geometrie-Feld").setEditorComponent(new TextField(), (item, vvalue) -> item.geomfield = vvalue);
		grid.addColumn(item -> item.evalfn).setCaption("Auswertungs-Funktion (Javascript)")
			.setEditorComponent(new TextArea(), (item, vvalue) -> item.geomfield = vvalue).setMinimumWidth(100);
		grid.addColumn(item -> item.radius).setCaption("Radius");
		
		DataProvider<ProfileLayer, ?> dataProvider = new ListDataProvider<ProfileLayer>(profile.profileLayer);
		grid.setDataProvider(dataProvider);
	}

}
