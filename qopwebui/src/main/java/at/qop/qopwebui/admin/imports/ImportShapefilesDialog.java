package at.qop.qopwebui.admin.imports;

import java.util.List;

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
	
	final List<ImportShapefileCMD> shapeFiles;
	
	public ImportShapefilesDialog(String title, List<ImportShapefileCMD> shapeFiles)
	{
		super(title);
		this.shapeFiles = shapeFiles;
		this.setModal(true);
		this.setWidth(780, Unit.PIXELS);
		this.setHeight(480, Unit.PIXELS);
		VerticalLayout subContent = new VerticalLayout();
		subContent.setSizeFull();
		this.setContent(subContent);
		
		Grid<ImportShapefileCMD> grid = new Grid<ImportShapefileCMD>();
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
		
		grid.addColumn(item -> item.warning).setCaption("Warning");
		
		DataProvider<ImportShapefileCMD, ?> dataProvider = new ListDataProvider<ImportShapefileCMD>(
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
		okButton.setEnabled(true);
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
