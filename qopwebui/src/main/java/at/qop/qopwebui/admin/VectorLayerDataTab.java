package at.qop.qopwebui.admin;

import java.util.stream.Collectors;

import com.vaadin.ui.ListSelect;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.dbconnector.metadata.QopDBMetadata;
import at.qop.qoplib.dbconnector.metadata.QopDBTable;
import at.qop.qoplib.domains.IGenericDomain;
import at.qop.qopwebui.admin.imports.ImportFilesComponent;
import at.qop.qopwebui.admin.imports.shape.ImportShapefilesComponent;

public class VectorLayerDataTab extends AbstractLayerDataTab {

	protected String baseSql(QopDBTable table) {
		return "select * from " + table.name;
	}
	
	protected String countSQL(QopDBTable table) {
		return "select count(gid) from " + table.name;
	}

	protected ImportFilesComponent importFilesComponent() {
		return new ImportShapefilesComponent();
	}
	
	protected void refreshList(IGenericDomain gd, ListSelect<QopDBTable> listSelect) {
		QopDBMetadata meta = gd.getMetadata();
		listSelect.setItems(meta.tables.stream().filter(t->t.isGeometric()).collect(Collectors.toList()));
	}
	
	protected Object stringRepresentation(DbTable table, final int i, DbRecord item) {
		
		int coltype = table.sqlTypes[i];
		if ("geometry".equals(table.typeNames[i]))
		{
			WKBReader wkbReader = new WKBReader();  
			try {
				return wkbReader.read(WKBReader.hexToBytes(String.valueOf(item.values[i])));
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		
		
		return item.values[i];
	}	

	
}
