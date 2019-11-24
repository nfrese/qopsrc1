package at.qop.qopwebui.admin.imports.raster;

import java.nio.file.Path;

import at.qop.qopwebui.admin.imports.ImportFileCMD;
import at.qop.qopwebui.admin.imports.ImportFilesComponent;

public class ImportRasterfilesComponent extends ImportFilesComponent {

	private static final long serialVersionUID = 1L;

	protected boolean useFile(Path file) {
		boolean tif = file.getFileName().toString().toLowerCase().endsWith(".tif");
		boolean tiff = file.getFileName().toString().toLowerCase().endsWith(".tiff");
		return tif || tiff;
	}
	
	protected ImportFileCMD importerInstance(Path file) {
		return new ImportRasterfileCMD(file);
	}

	protected String what() {
		return "Raster-Dateien";
	}
	
}
