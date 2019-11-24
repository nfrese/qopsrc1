package at.qop.qopwebui.admin.forms.exports.shape;

import java.nio.file.Path;
import java.util.List;

import at.qop.qopwebui.admin.forms.exports.ExportFileCMD;
import at.qop.qopwebui.admin.forms.exports.ExportFiles;

public class ExportShapefiles extends ExportFiles {

	public ExportShapefiles(List<String> tableNames) {
		super(tableNames);
	}

	@Override
	protected String what() {
		return "Shape-Dateien";
	}
	
	protected String downloadFileName() {
		return "downloadshapes.zip";
	}
	
	protected ExportFileCMD exportFileCMD(String tableName, Path path) {
		return new ExportShapefileCMD(path , tableName);
	}

}
