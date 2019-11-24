package at.qop.qopwebui.admin.imports.shape;

import java.nio.file.Path;

import at.qop.qopwebui.admin.imports.ImportFileCMD;
import at.qop.qopwebui.admin.imports.ImportFilesComponent;

public class ImportShapefilesComponent extends ImportFilesComponent {

	private static final long serialVersionUID = 1L;

	protected boolean useFile(Path file) {
		return file.getFileName().toString().toLowerCase().endsWith(".shp");
	}
	
	protected ImportFileCMD importerInstance(Path file) {
		return new ImportShapefileCMD(file);
	}

	protected String what() {
		return "Shape-Dateien";
	}
	
}
