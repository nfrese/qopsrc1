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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.vaadin.tatu.TwinColSelect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;

import at.qop.qoplib.Constants;
import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.Utils;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileAnalysis;
import at.qop.qoplib.extinterfaces.json.ExportProfile;
import at.qop.qopwebui.admin.forms.ProfileForm;
import at.qop.qopwebui.admin.forms.exports.DumpDatabase;
import at.qop.qopwebui.admin.forms.exports.DumpDatabaseAppendScript;
import at.qop.qopwebui.components.ConfirmationDialog;
import at.qop.qopwebui.components.DownloadDialog;

public class ProfilesTab extends AbstractTab {

	private static final String BSLNL = "\n";
	private Profile currentProfile;
	private boolean twinSelectSilent = false;

	@Override
	public Component initialize(Page page) {
		
		MultiSelectListBox<Profile> listSelect = new MultiSelectListBox<Profile>(); // TODO "Profil auswählen..."
        //listSelect.setRows(15);
        refreshProfileList(listSelect);
        
        Button addProfileButton = new Button("Profil hinzufügen...", VaadinIcon.PLUS.create());
        addProfileButton.addClickListener(e -> {

        	Profile profile = new Profile();
        	
        	new ProfileForm("Neues Profil", profile, true).ok(e3 -> {
    			LookupSessionBeans.profileDomain().createProfile(profile);
    			refreshProfileList(listSelect);
        	}).show();

        });
        
        Button editProfileButton = new Button("Profil bearbeiten..." , VaadinIcon.EDIT.create());
        editProfileButton.setEnabled(false);
        editProfileButton.addClickListener(e -> {

        	Set<Profile> sel = listSelect.getSelectedItems();
        	if (sel.size() == 1)
        	{
        		Profile profile = sel.iterator().next();

        		new ProfileForm("Profil bearbeiten", profile, false).ok(e3 -> {
        			LookupSessionBeans.profileDomain().updateProfile(profile);
        			refreshProfileList(listSelect);
        		}).show();
        	}

        });
        
        Button cloneProfileButton = new Button("Profil klonen...", VaadinIcon.QUOTE_RIGHT.create());
        cloneProfileButton.setEnabled(false);
        cloneProfileButton.addClickListener(e -> {

        	Set<Profile> sel = listSelect.getSelectedItems();
        	if (sel.size() == 1)
        	{
        		Profile profile = sel.iterator().next();
        		Profile clone;
				try {
					clone = Utils.deepClone(profile);
				} catch (ClassNotFoundException | IOException e1) {
					throw new RuntimeException(e1);
				}
        		clone.name = clone.name + "_" + ((int)(Math.random()*100));
        		
        		clone.profileAnalysis.forEach(pa -> {
        			pa.id = 0;
        			pa.profile = clone;
        		});

        		new ProfileForm("Profil klonen", clone, true).ok(e3 -> {
        			LookupSessionBeans.profileDomain().createProfile(clone);
        			LookupSessionBeans.profileDomain().createProfileAnalysis(new HashSet<>(clone.profileAnalysis));
        			refreshProfileList(listSelect);
        		}).show();
        	}
        });
        
        Button removeProfileButton = new Button("Profil löschen...", VaadinIcon.TRASH.create());
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
 
        Button profileAsJsonButton = new Button("Profile as JSON", VaadinIcon.DOWNLOAD.create());
        profileAsJsonButton.setEnabled(false);
        profileAsJsonButton.addClickListener(e -> {
        	Set<Profile> sel = listSelect.getSelectedItems();
        	if (sel.size() == 1)
        	{
        		Profile profile = sel.iterator().next();
        		
				try {
					final String json = new ExportProfile().asJson(profile);
					System.out.println(json);
					
					InputStreamFactory source = new InputStreamFactory() {
						private static final long serialVersionUID = 1L;

						@Override
						public InputStream createInputStream() {
							try {
								return new ByteArrayInputStream(json.getBytes("UTF-8"));
							} catch (UnsupportedEncodingException e) {
								throw new RuntimeException(e);
							}
						}
					};
					
					StreamResource resource = new StreamResource("export_profile_" + profile.name + ".json", source);
					DownloadDialog dd = new DownloadDialog("Downlaod JSON", "Profiles", resource);
					dd.show();

					
				} catch (JsonProcessingException e1) {
					throw new RuntimeException(e1);
				}
        	}
        });
        
        Button profileExportButton = new Button("Export Database for selected Profiles", VaadinIcon.INFO.create());
        profileExportButton.setEnabled(false);
        profileExportButton.addClickListener(e -> {
        	
        	if (listSelect.getSelectedItems().size() > 0)
        	{
        		StringBuilder sqlSb = new StringBuilder();
        		

        		Set<String> collectedTablenames = new TreeSet<>();
        		Set<String> collectedProfileNames = new TreeSet<>();
        		Set<String> collectedAnalysisNames = new TreeSet<>();
        		Set<String> collectedAnalysisFunctionNames = new TreeSet<>();
        		
        		collectedProfileNames.addAll(listSelect.getSelectedItems().stream().map(profile -> profile.name).collect(Collectors.toList()));

        		listSelect.getSelectedItems().stream().forEach(profile -> {
        			System.out.println("******* collecting tablenames for profile " + profile.name); 

        			profile.profileAnalysis.forEach(pa -> {
        				String tableName = Utils.guessTableName(pa.analysis.query);
        				System.out.println(pa.analysis.name + " -> " + tableName);
        				collectedTablenames.add(tableName);
        				collectedAnalysisNames.add(pa.analysis.name);
        				collectedAnalysisFunctionNames.add(pa.analysis.analysisfunction.name);
        				
        			});
        			
        		});

//        		for (String tableName : collectedTablenames)
//        		{
//        			sqlSb.append("DELETE * FROM " + tableName + " INTO qopexport." + tableName);
//        			sqlSb.append("\n");
//        		}

        		//sqlSb.append(BSLNL);

        		String inProfileList = collectedProfileNames.stream().map(name -> "'" + name + "'").collect( Collectors.joining( ", " ) );
        		sqlSb.append("DELETE FROM public.q_profileanalysis where profile_name not in (" + inProfileList + ");");
        		//sqlSb.append(BSLNL);
        		sqlSb.append("DELETE FROM public.q_profile where name not in (" + inProfileList + ");");
        		//sqlSb.append(BSLNL);
        		
        		String inAnalysisIdList = collectedAnalysisNames.stream().map(id -> "'" + id + "'").collect( Collectors.joining( ", " ) );
				sqlSb.append("DELETE FROM public.q_analysis where name not in (" + inAnalysisIdList + ");");
				//sqlSb.append(BSLNL);
				

				String inAnalysesFunctionList = collectedAnalysisFunctionNames.stream().map(name -> "'" + name + "'").collect( Collectors.joining( ", " ) );
				
				sqlSb.append("DELETE FROM public.q_analysisfunction where name not in (" + inAnalysesFunctionList + ");");
				//sqlSb.append(BSLNL);
				
				System.out.println(sqlSb);
				
        		collectedTablenames.add(Constants.Q_ADDRESSES);
        		collectedTablenames.addAll(Constants.CONFIG_TABLES); 

        		DumpDatabase dd = new DumpDatabaseAppendScript(new ArrayList<>(collectedTablenames), sqlSb.toString());
        		dd.run();
        	}

        });
        
        TwinColSelect<ProfileAnalysis> twinSelect =
        	    new TwinColSelect<>(); // TODO "Auswertungen hinzufügen"

      	twinSelect.setHeight(80, Unit.PERCENTAGE);
        
    	Grid<ProfileAnalysis> grid = new Grid<ProfileAnalysis>(); // "Kategorien festlegen" TOdo
    	//grid.getEditor().setEnabled(true);
        grid.setWidth(100.0f, Unit.PERCENTAGE);
        grid.setHeight(100.0f, Unit.PERCENTAGE);
        grid.addColumn(item -> item.analysis.name).setHeader("Name");
		grid.addColumn(item -> item.analysis.description).setHeader("Beschreibung");
		grid.addColumn(item -> item.weight  +"").setHeader("Gewicht").setEditorComponent((item)  -> { 
			return new TextField((v)  -> { 
					item.weight = Double.valueOf(v.getValue()); 
					LookupSessionBeans.profileDomain().updateProfileAnalysis(item);
				});
		});
		grid.addColumn(item -> item.category).setHeader("Kategorie/Sortierung (zb 1.01)")
			.setEditorComponent( 
				(item)  -> { 
					return new TextField(item.category,
							v -> { 
								item.category = v.getValue(); 
								LookupSessionBeans.profileDomain().updateProfileAnalysis(item);
							}
							);
				});
		
		grid.addColumn(item -> item.categorytitle).setHeader("Kategorie-Titel (zb Ambience)")
		.setWidth("190px")
		.setEditorComponent((item)  -> { 
			return new TextField(item.categorytitle,
					v -> { 
				item.categorytitle = v.getValue(); 
				LookupSessionBeans.profileDomain().updateProfileAnalysis(item);
			});
			
	});

		grid.addColumn(item -> item.ratingvisible).setHeader("Einzel-Bewertung sichtbar")
		.setEditorComponent((item)  -> {return new Checkbox(item.ratingvisible, 
			(v)  -> { 
				item.ratingvisible = v.getValue(); 
				LookupSessionBeans.profileDomain().updateProfileAnalysis(item);
			});
		});
		
    	twinSelect.addSelectionListener(event -> {
    		if (!twinSelectSilent)
    		{

    			LookupSessionBeans.profileDomain().createProfileAnalysis(event.getAddedSelection());
    			LookupSessionBeans.profileDomain().removeProfileAnalysis(event.getRemovedSelection());
    			updateTwinSelect(twinSelect);
    			refreshGrid(grid);
    		}
    			
    	});
        
		listSelect.addValueChangeListener(
				event -> { 
					removeProfileButton.setEnabled(event.getValue().size() == 1);
					editProfileButton.setEnabled(event.getValue().size() == 1);
					cloneProfileButton.setEnabled(event.getValue().size() == 1);
					profileAsJsonButton.setEnabled(event.getValue().size() == 1);
					profileExportButton.setEnabled(event.getValue().size() > 0);
					
					if (event.getValue().size() == 1)
					{
						currentProfile = event.getValue().iterator().next();
					}
					else
					{
						currentProfile = null;
					}
					updateTwinSelect(twinSelect);
					refreshGrid(grid);
				} );
		grid.setSizeFull();
		
		listSelect.setSizeFull();
		listSelect.setWidth(220, Unit.PIXELS);
		
		twinSelect.setWidth(100, Unit.PERCENTAGE);
		
		VerticalLayout vl = new VerticalLayout(listSelect, 
				addProfileButton, 
				editProfileButton, 
				cloneProfileButton, 
				removeProfileButton,
				profileExportButton,
				profileAsJsonButton);
		//vl.setExpandRatio(listSelect, 5.0f); // TODO
		vl.setMargin(false);
		vl.setHeight(100, Unit.PERCENTAGE);
		vl.setWidth(240, Unit.PIXELS);
		final HorizontalLayout hl = new HorizontalLayout( 
				vl, twinSelect, grid);
//		hl.setExpandRatio(grid, 1.7f);
		//hl.setExpandRatio(twinSelect, 1f); TODO
		hl.setMargin(true);
		hl.setSizeFull();
		
		SplitLayout hsp = new SplitLayout(hl, grid);
		//hsp.setSplitPosition(50, Unit.PERCENTAGE); TODO
		hsp.setSizeFull();
		return hsp;
	}

	private void updateTwinSelect(TwinColSelect<ProfileAnalysis> twinSelect) {
		
		twinSelectSilent = true;
		if (currentProfile == null)
		{
			twinSelect.setItems(Collections.emptyList());
			return;
		}
		
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

		twinSelect.setItemLabelGenerator(item -> item.analysis.name);
		twinSelectSilent = false;
	}

	private void refreshProfileList(MultiSelectListBox<Profile> listSelect) {
		listSelect.setItems(LookupSessionBeans.profileDomain().listProfiles());
	}

	private void refreshGrid(Grid<ProfileAnalysis> grid) {
		if (currentProfile != null)
		{
			Comparator<ProfileAnalysis> comparator = profileAnalysisComperator();
			
			DataProvider<ProfileAnalysis, ?> dataProvider = new ListDataProvider<ProfileAnalysis>(
					currentProfile.profileAnalysis.stream().sorted(comparator).collect(Collectors.toList()));
			
			grid.setDataProvider(dataProvider);
		}
		else
		{
			DataProvider<ProfileAnalysis, ?> dataProvider = new ListDataProvider<ProfileAnalysis>(
					Collections.emptyList());
			
			grid.setDataProvider(dataProvider);
		}
	}

	private static Comparator<ProfileAnalysis> profileAnalysisComperator() {
		Comparator<ProfileAnalysis> comparator = Comparator.comparing(pa -> pa.category + "");
		comparator = comparator.thenComparing(Comparator.comparing(pa -> pa.analysis.name + ""));
		return comparator;
	}

}
