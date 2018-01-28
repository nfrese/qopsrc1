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
	
	private static final long serialVersionUID = 1L;
	
	Button closeButton;
	Button cancelButton;

	private HorizontalLayout hlButtons;
	
	final List<ImportShapefile> shapeFiles;
	
	public ImportShapefilesDialog(String title, List<ImportShapefile> shapeFiles)
	{
		super(title);
		this.shapeFiles = shapeFiles;
		this.setModal(true);
		this.setWidth(780, Unit.PIXELS);
		this.setHeight(480, Unit.PIXELS);
		VerticalLayout subContent = new VerticalLayout();
		subContent.setSizeFull();
		this.setContent(subContent);
		
		Grid<ImportShapefile> grid = new Grid<ImportShapefile>();
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
		
		grid.addColumn(item -> item.encoding).setCaption("Encoding");
		
		grid.addColumn(item -> item.warning).setCaption("Warning");
		
		DataProvider<ImportShapefile, ?> dataProvider = new ListDataProvider<ImportShapefile>(
				shapeFiles);
		
		grid.setDataProvider(dataProvider);
		grid.getEditor().setEnabled(true);
		
		subContent.addComponent(grid);
		subContent.setExpandRatio(grid, 10.0f);
		cancelButton = new Button("Abbruch", VaadinIcons.CLOSE);
		cancelButton.addClickListener(e2 -> {
			this.close();
		});
		closeButton = new Button("OK", VaadinIcons.CHECK);
		closeButton.setEnabled(true);
		closeButton.addClickListener(e2 -> {
			onDone.run();
			this.close(); 
		});

		this.setClosable(true);
		UI.getCurrent().setPollInterval(1000);
		hlButtons = new HorizontalLayout(cancelButton, closeButton);
		subContent.addComponent(hlButtons);

	}
}
