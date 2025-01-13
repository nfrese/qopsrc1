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

package at.qop.qopwebui.admin.imports;

import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;

import at.qop.qopwebui.components.AbstractDialog;

public class ImportShapefilesDialog extends AbstractDialog {
	
	public Runnable onDone = () -> {};
	public Runnable onExit = () -> {};
	
	private static final long serialVersionUID = 1L;
	
	Button okButton;
	Button cancelButton;
	
	private HorizontalLayout hlButtons;
	
	final List<ImportFileCMD> shapeFiles;
	
	public ImportShapefilesDialog(String title, List<ImportFileCMD> shapeFiles)
	{
		super(title);
		this.shapeFiles = shapeFiles;
		this.setModal(true);
		this.setWidth(780, Unit.PIXELS);
		this.setHeight(480, Unit.PIXELS);
		VerticalLayout subContent = new VerticalLayout();
		subContent.setSizeFull();
		this.add(subContent);
		
		Grid<ImportFileCMD> grid = new Grid<ImportFileCMD>();
		grid.setSizeFull();
		
		grid.addColumn(item -> item.importFlag).setHeader("Wird importiert")
		.setEditorComponent(item -> { return new Checkbox(item.importFlag, 
				(v)  -> { 
					item.importFlag = v.getValue(); 
				});});
		
		grid.addColumn(item -> item.getFilename()).setHeader("Shape");
		grid.addColumn(item -> item.tableName).setHeader("als Tabelle")
		.setEditorComponent(item -> { return new TextField( 
			(v)  -> { 
				item.tableName = v.getValue(); 
			});});
		
		grid.addColumn(item -> item.srid + "").setHeader("SRID"); /*.setEditorComponent(new TextField(), 
				(item,v)  -> { 
					item.srid = Integer.valueOf(v); 
				});*/
		
		grid.addColumn(item -> item.encoding).setHeader("Encoding (siehe CPG oder CST-Datei)")
		.setEditorComponent(item -> { return new TextField( 
				(v)  -> { 
					item.encoding = v.getValue(); 
				});	});	
		
		grid.addColumn(item -> item.warnings.stream().collect(Collectors.joining(";"))).setHeader("Warning");
		grid.addColumn(item -> item.error).setHeader("Error");
		
		DataProvider<ImportFileCMD, ?> dataProvider = new ListDataProvider<ImportFileCMD>(
				shapeFiles);
		
		grid.setDataProvider(dataProvider);
		//grid.getEditor().setEnabled(true); TODO
		
		subContent.add(grid);
		//subContent.setExpandRatio(grid, 10.0f); TODO
		cancelButton = new Button("Schließen", VaadinIcon.CLOSE.create());
		cancelButton.addClickListener(e2 -> {
			onExit.run();
			this.close();
		});
		okButton = new Button("Weiter", VaadinIcon.ARROW_RIGHT.create());
		okButton.setEnabled(shapeFiles.stream().allMatch(item -> item.isValid()));
		okButton.addClickListener(e2 -> {
			onDone.run();
			this.close(); 
		});

		this.setCloseOnEsc(false);
		this.setCloseOnOutsideClick(false);
		UI.getCurrent().setPollInterval(1000);
		hlButtons = new HorizontalLayout(cancelButton, okButton);
		subContent.add(hlButtons);

	}
}
