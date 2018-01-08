package at.qop.qopwebui.admin;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.server.Page;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.domains.ProfileDomain;
import at.qop.qoplib.entities.Analysis;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileAnalysis;
import at.qop.qopwebui.admin.forms.ProfileForm;
import at.qop.qopwebui.components.ConfirmationDialog;

public class ProfilesTab extends AbstractTab {

	private Profile currentProfile;
	private boolean twinSelectSilent = false;

	@Override
	public Component initialize(Page page) {
		
		ListSelect<Profile> listSelect = new ListSelect<Profile>("Profil auswählen...");
        listSelect.setRows(15);
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
 
        TwinColSelect<ProfileAnalysis> twinSelect =
        	    new TwinColSelect<>("Auswertungen hinzufügen");

      	twinSelect.setRows(15);
        
        TextField weightField = new TextField();  
        
    	// Handle value changes
    	twinSelect.addSelectionListener(event -> {
    		if (!twinSelectSilent)
    		{

    			LookupSessionBeans.profileDomain().createProfileAnalysis(event.getAddedSelection());
    			LookupSessionBeans.profileDomain().removeProfileAnalysis(event.getRemovedSelection());
    			updateTwinSelect(twinSelect);
    		}
    			
    	});
        
        final HorizontalLayout hl = new HorizontalLayout(listSelect, new VerticalLayout(addProfileButton, editProfileButton, removeProfileButton), twinSelect);
        hl.setMargin(true);
        hl.setHeight(100.0f, Unit.PERCENTAGE);
        Grid<Analysis> grid = new Grid<Analysis>();
        grid.setWidth(100.0f, Unit.PERCENTAGE);
        grid.setHeight(100.0f, Unit.PERCENTAGE);
        
        
		listSelect.addValueChangeListener(
				event -> { 
					removeProfileButton.setEnabled(event.getValue().size() == 1);
					editProfileButton.setEnabled(event.getValue().size() == 1);
					
					if (event.getValue().size() == 1)
					{
						currentProfile = event.getValue().iterator().next();
						
						updateTwinSelect(twinSelect);
					}
					else
					{
						currentProfile = null;
						twinSelect.setItems(Collections.emptyList());
					}
				} );
		
		
		return hl;
	}

	private void updateTwinSelect(TwinColSelect<ProfileAnalysis> twinSelect) {
		
		twinSelectSilent = true;
		
		currentProfile = LookupSessionBeans.profileDomain().listProfiles().stream().filter(p -> p.name.equals(currentProfile.name)).findFirst().get();
		
		List<ProfileAnalysis> allPaS = LookupSessionBeans.profileDomain().listAnalyses().stream().map(a -> { 
			ProfileAnalysis pa = new ProfileAnalysis();
			pa.analysis = a;
			pa.profile = currentProfile;
			return pa;
		}).collect(Collectors.toList());
		
		Set<String> currentAnalysis = currentProfile.profileAnalysis.stream().map(pa -> pa.analysis.name).collect(Collectors.toSet());
		Set<ProfileAnalysis> all = allPaS.stream().filter(pa -> !currentAnalysis.contains(pa.analysis.name)).collect(Collectors.toSet());
		all.addAll(currentProfile.profileAnalysis);
		twinSelect.setItems(all);
		
		twinSelect.updateSelection(currentProfile.profileAnalysis.stream().collect(Collectors.toSet())
				, Collections.emptySet());

		twinSelect.setItemCaptionGenerator(item -> item.analysis.name);
		twinSelectSilent = false;
	}

	private void refreshProfileList(ListSelect<Profile> listSelect) {
		listSelect.setItems(LookupSessionBeans.profileDomain().listProfiles());
	}


}
