package at.qop.qopwebui.admin.forms.exports.raster;

import java.nio.file.Path;
import java.util.List;

import at.qop.qopwebui.admin.forms.exports.ExportFileCMD;
import at.qop.qopwebui.admin.forms.exports.ExportFiles;
import at.qop.qopwebui.admin.forms.exports.shape.ExportShapefileCMD;

public class ExportRasters extends ExportFiles {

	public ExportRasters(List<String> tableNames) {
		super(tableNames);
	}
	
	@Override
	protected String what() {
		return "Raster-Dateien";
	}
	
	protected String downloadFileName() {
		return "downloadrasters.zip";
	}
	
	protected ExportFileCMD exportFileCMD(String tableName, Path path) {
		return new ExportRasterCMD(path , tableName);
	}

}
