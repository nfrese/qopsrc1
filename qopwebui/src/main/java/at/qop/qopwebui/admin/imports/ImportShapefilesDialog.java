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

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

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
		this.setContent(subContent);
		
		Grid<ImportFileCMD> grid = new Grid<ImportFileCMD>();
		grid.setSizeFull();
		
		grid.addColumn(item -> item.importFlag).setCaption("Wird importiert")
		.setEditorComponent(new CheckBox(), 
				(item,v)  -> { 
					item.importFlag = v; 
				});
		
		grid.addColumn(item -> item.getFilename()).setCaption("Shape");
		grid.addColumn(item -> item.tableName).setCaption("als Tabelle")
		.setEditorComponent(new TextField(), 
			(item,v)  -> { 
				item.tableName = v; 
			});
		
		grid.addColumn(item -> item.srid + "").setCaption("SRID"); /*.setEditorComponent(new TextField(), 
				(item,v)  -> { 
					item.srid = Integer.valueOf(v); 
				});*/
		
		grid.addColumn(item -> item.encoding).setCaption("Encoding (siehe CPG oder CST-Datei)")
		.setEditorComponent(new TextField(), 
				(item,v)  -> { 
					item.encoding = v; 
				});		
		
		grid.addColumn(item -> item.warnings.stream().collect(Collectors.joining(";"))).setCaption("Warning");
		grid.addColumn(item -> item.error).setCaption("Error");
		
		DataProvider<ImportFileCMD, ?> dataProvider = new ListDataProvider<ImportFileCMD>(
				shapeFiles);
		
		grid.setDataProvider(dataProvider);
		grid.getEditor().setEnabled(true);
		
		subContent.addComponent(grid);
		subContent.setExpandRatio(grid, 10.0f);
		cancelButton = new Button("Schließen", VaadinIcons.CLOSE);
		cancelButton.addClickListener(e2 -> {
			onExit.run();
			this.close();
		});
		okButton = new Button("Weiter", VaadinIcons.ARROW_RIGHT);
		okButton.setEnabled(shapeFiles.stream().allMatch(item -> item.isValid()));
		okButton.addClickListener(e2 -> {
			onDone.run();
			this.close(); 
		});

		this.setClosable(false);
		UI.getCurrent().setPollInterval(1000);
		hlButtons = new HorizontalLayout(cancelButton, okButton);
		subContent.addComponent(hlButtons);

	}
}
