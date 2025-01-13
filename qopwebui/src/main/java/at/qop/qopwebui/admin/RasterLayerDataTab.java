package at.qop.qopwebui.admin;

import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.listbox.MultiSelectListBox;

import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.dbconnector.metadata.QopDBMetadata;
import at.qop.qoplib.dbconnector.metadata.QopDBTable;
import at.qop.qoplib.domains.IGenericDomain;
import at.qop.qopwebui.admin.forms.exports.ExportFiles;
import at.qop.qopwebui.admin.forms.exports.raster.ExportRasters;
import at.qop.qopwebui.admin.imports.ImportFilesComponent;
import at.qop.qopwebui.admin.imports.raster.ImportRasterfilesComponent;

public class RasterLayerDataTab extends AbstractLayerDataTab {

	protected String baseSql(QopDBTable table) {
		return 
	     "SELECT rid, (foo.md).*, mem, filename"
	     + " FROM (SELECT rid, ST_MetaData(rast) As md, pg_column_size(rast) as mem, filename"
		 + " FROM " + table.nameSQLQuoted() + " "
        + " ) As foo ";
	}
	
	protected String countSQL(QopDBTable table) {
		return "select count(rid) from " + table.nameSQLQuoted() + " ";
	}
	
	protected ImportFilesComponent importFilesComponent() {
		return new ImportRasterfilesComponent();
	}
	
	protected ExportFiles exportTables(List<String> tableNames) {
		return new ExportRasters(tableNames);
	}
	
	protected void refreshList(IGenericDomain gd, MultiSelectListBox<QopDBTable> listSelect) {
		QopDBMetadata meta = gd.getMetadata();
		listSelect.setItems(meta.tables.stream().filter(t->t.isRaster()).collect(Collectors.toList()));
	}
	

	
	protected Object stringRepresentation(DbTable table, final int i, DbRecord item) {
		
//		int coltype = table.sqlTypes[i];
//		if ("raster".equals(table.typeNames[i]))
//		{
//			Object data = item.values[i];
//			return "010101";
//		}
		
		return item.values[i];
	}	

	
}
