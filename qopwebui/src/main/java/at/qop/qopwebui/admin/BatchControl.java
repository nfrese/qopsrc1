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

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.batch.BatchCalculation;
import at.qop.qoplib.dbconnector.metadata.QopDBMetadata;
import at.qop.qoplib.domains.IGenericDomain;
import at.qop.qoplib.entities.Profile;
import at.qop.qopwebui.components.ExceptionDialog;
import at.qop.qopwebui.components.InfoDialog;

public class BatchControl {

	private Profile currentProfile;
	private Label batchInfoLabel;
	private Button batchButton;
	private Button cancelButton;
	private ComboBox<Profile> profileCombo;
	private ComboBox<String> pointsTableCombo;
	private TextField nameColumnTextField;
	private ProgressBar progressBar;
	private Label progressLabel;

	public static BatchCalculation bc = null;

	public Component init() {
		UI currentUI = UI.getCurrent();
		progressBar = new ProgressBar();
		progressBar.setValue(Float.NaN);

		if (bc != null)
		{
			bc.cancelled = true;
		}

		IGenericDomain gd = LookupSessionBeans.genericDomain();
		QopDBMetadata meta = gd.getMetadata();
		pointsTableCombo = new ComboBox<>("Points Table");
		pointsTableCombo.setItems(meta.tables.stream().filter(t->t.isGeometric()).map(t->t.name).collect(Collectors.toList()));		
		
		nameColumnTextField = new TextField("Caption column");
		nameColumnTextField.setValue("name");
		
		progressLabel = new Label("", ContentMode.HTML);
		batchInfoLabel = new Label("", ContentMode.HTML);

		batchButton = new Button("Batch Calculation");
		batchButton.addClickListener(e -> {

			if (currentProfile != null)
			{
				if (bc != null) throw new RuntimeException("dont!");

				String pointTableName = pointsTableCombo.getSelectedItem().get();
				String geomFieldName = "geom";
				String nameFieldName = nameColumnTextField.getValue();
				
				bc = new BatchCalculation(currentProfile, pointTableName, geomFieldName, nameFieldName) {
					@Override
					protected void progress(int overall_, int count_) {
						super.progress(overall_, count_);
						currentUI.access( new Runnable() {
							public void run() {
								progressBar.setValue((float)count_/(float)overall_);
								
								int percent = (100* count)/overall_;
								String s = "Progress: " + count_ + "/" + overall_ + " = " + percent + "%";
								progressLabel.setValue(s);
							}}
						);
					}

					protected void failed(Throwable t)
					{
						currentUI.access( new Runnable() {
							public void run() {
								if (t instanceof CancellationException)
								{
									new InfoDialog("Info", "Batch abgebrochen").show();
									batchInfoLabel.setValue(batchInfoLabel.getValue() + "<br><b>Abgebrochen!</b>");
								}
								else
								{
									new ExceptionDialog("Batch fehlgeschlagen", t).show();
									batchInfoLabel.setValue(batchInfoLabel.getValue() + "<br><b>Fehlgeschlagen!</b>");
								}
								updateButtons();
								currentUI.setPollInterval(-1);
							}
						});

						bc = null;
					}

					protected void success()
					{
						currentUI.access( new Runnable() {
							public void run() {

								batchInfoLabel.setValue(batchInfoLabel.getValue() + "<br><b>Erfolgreich!</b>");
								updateButtons();
								currentUI.setPollInterval(-1);
							}
						});
						bc = null;
					}
				};

				Thread t = LookupSessionBeans.genericDomain().getThreadFactory().newThread(bc);
				t.start();
				updateButtons();
				currentUI.setPollInterval(500);
				batchInfoLabel.setValue("Batch Verarbeitung Profil " + currentProfile.name + "<br>Tabelle batch_" + currentProfile.name + " wird geschrieben!");
			}
		});

		cancelButton = new Button("Cancel");
		cancelButton.addClickListener(e -> {
			if (bc != null)
			{
				bc.cancelled = true;
				bc = null;
			}
		});

		List<Profile> profiles = LookupSessionBeans.profileDomain().listProfiles();
		profileCombo = new ComboBox<>("Profilauswahl", profiles);
		if (profiles.size() > 0)
		{
			profileCombo.setSelectedItem(profiles.get(0));
			currentProfile = profiles.get(0);
		}
		profileCombo.setEmptySelectionAllowed(false);
		profileCombo.setTextInputAllowed(false);

		profileCombo.addSelectionListener(event -> {
			currentProfile = event.getSelectedItem().isPresent() ? event.getSelectedItem().get() : null;
		});

		updateButtons();

		return new VerticalLayout(new HorizontalLayout(profileCombo, this.pointsTableCombo, this.nameColumnTextField), new HorizontalLayout(batchButton, cancelButton, progressBar, progressLabel), batchInfoLabel);

	}

	private void updateButtons()
	{
		this.batchButton.setEnabled(bc == null);
		this.profileCombo.setEnabled(bc == null);
		this.cancelButton.setEnabled(bc != null);
		this.batchInfoLabel.setEnabled(bc != null);
		this.progressBar.setEnabled(bc != null);
	}

}
