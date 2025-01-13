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

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.textfield.TextField;

import at.qop.qoplib.Constants;
import at.qop.qoplib.TmpWorkingDir;
import at.qop.qoplib.imports.PerformAddressUpdate;
import at.qop.qopwebui.admin.forms.exports.DumpDatabase;
import at.qop.qopwebui.admin.imports.ImportFilesComponent;
import at.qop.qopwebui.components.ExecDialog;

public class MiscTab extends AbstractTab {

	@Override
	public Component initialize(Page page) {
		
		final VerticalLayout vl = new VerticalLayout();
		
		{
			Span p = new Span();
			TextField tfBezirkFilter = new TextField("Bezirkfilter (zb: 01)");
			Button button = new Button("Adressen aktualisieren");
			button.addClickListener(e -> {
				new Notification("Lade die Adressen herunter").open();
				new PerformAddressUpdate().updateAddresses(tfBezirkFilter.getValue());
			});
			HorizontalLayout hl = new HorizontalLayout(button, tfBezirkFilter);
			hl.setMargin(true);
			p.add(hl);
			vl.add(p);
		}
		{
			Span p = new Span();
			p.add(new BatchControl().init());
			vl.add(p);
		}
		{
			Span p = new Span();
			Button dumpConfigButton = new Button("Konfigurationstabellen sichern");
			dumpConfigButton.addClickListener(e -> {
				DumpDatabase dd = new DumpDatabase(Constants.CONFIG_TABLES);
				dd.run();
			});

			Button dumpAllButton = new Button("Komplette Datenbank sichern");
			dumpAllButton.addClickListener(e -> {
				DumpDatabase dd = new DumpDatabase();
				dd.run();
			});
			
			Button systemInfoButton = new Button("System-Info");
			systemInfoButton.addClickListener(e -> {
				ExecDialog execDialog = new ExecDialog("System-Info");
				List<String> commands = new ArrayList<>();
				commands.add("uname -a");
				commands.add("set");
				commands.add("psql -V");
				commands.add("pg_dump -V");
				commands.add("pgsql2shp");
				commands.add("shp2pgsql");
				commands.add("zip -v");
				commands.add("unzip -v");
				
				TmpWorkingDir tmpDir = new TmpWorkingDir();
				tmpDir.create();
				
				execDialog.show();
				execDialog.executeCommands(commands.iterator(), null, tmpDir.dir, true);

			});
			
			HorizontalLayout hl = new HorizontalLayout(dumpConfigButton, dumpAllButton, systemInfoButton);
			hl.setMargin(true);
			p.add(hl);
			vl.add(p);
		}
		vl.setMargin(true);
		return vl;
	}

}
