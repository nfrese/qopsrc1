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

package at.qop.qopwebui.admin.imports.raster;

import java.nio.file.Path;

import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;

import at.qop.qoplib.Config;
import at.qop.qoplib.Utils;
import at.qop.qopwebui.admin.imports.ImportFileCMD;

public class ImportRasterfileCMD extends ImportFileCMD {
	

	private boolean reprojectionRequired = false;
	
	public ImportRasterfileCMD(Path path) {
		super(path);
	}
	
	public String getFilename()
	{
		return path.getFileName().toString();
	}
	
	private Path getTranslatedFilePath()
	{
		return path.getParent().resolve(path.getFileName() + "__translated4326.tif"); 
	}

	protected String proposeTableName() {
		return getFilename().toLowerCase().replaceAll("\\.tif$", "").replaceAll("\\.tiff$", "");
	}
	
	@Override
	public boolean isReprojectionRequired() {
		return reprojectionRequired;
	}
	
	@Override
	public void validate()
	{
		GeoTiffFormat format = new GeoTiffFormat();
		GeoTiffReader tiffReader = format.getReader(this.path +"");
		if (tiffReader != null && tiffReader.getCoordinateReferenceSystem() != null && tiffReader.getCoordinateReferenceSystem().getName() != null)
		{
			String code = tiffReader.getCoordinateReferenceSystem().getName().getCode();
			boolean sridOk = code.equals("GCS Name = WGS 84");
			if (!sridOk) warnings.add("Projection not WGS 84 (srid=4326). code: " + code + " -> reprojection required");
			reprojectionRequired = true;
		}
		else {
			warnings.add("validating crs - tiffReader == null -> reprojection required");
			reprojectionRequired = true;
		}
	}

	@Override
	public String reprojectCmd()
	{
		String cmd = "gdalwarp %SHAPEFILE% %REPROJ_SHAPEFILE% -t_srs \"+proj=longlat +ellps=WGS84\""; 
		cmd = cmd.replace("%SHAPEFILE%", Utils.uxCmdStringEscape(this.path +""));
		cmd = cmd.replace("%REPROJ_SHAPEFILE%", Utils.uxCmdStringEscape(getTranslatedFilePath() +""));
		return cmd;
	}
	
	public String cmd(Config cfgFile) {
		String schema = cfgFile.getDbSchema();
		
		String cmd = "raster2pgsql -s %SRID% -I -C -M %SHAPEFILE% -F -t 500x500 %TABLENAME% ";
		cmd += " | psql -h %HOST% -U %USER_NAME% -d %DB% -p %PORT%";
		
		cmd = cmd.replace("%SRID%", this.srid+"");
		cmd = cmd.replace("%ENCODING%", Utils.uxCmdStringEscape(this.encoding));
		if (reprojectionRequired)
		{
			cmd = cmd.replace("%SHAPEFILE%", Utils.uxCmdStringEscape(this.getTranslatedFilePath() +""));
		}
		else
		{
			cmd = cmd.replace("%SHAPEFILE%", Utils.uxCmdStringEscape(this.path +""));
		}
		cmd = cmd.replace("%TABLENAME%", Utils.uxCmdStringEscape(schema + "." + this.tableName));
		
		cmd = cmd.replace("%HOST%", Utils.uxCmdStringEscape(cfgFile.getDbHost()));
		cmd = cmd.replace("%USER_NAME%", Utils.uxCmdStringEscape(cfgFile.getDbUserName()));
		cmd = cmd.replace("%DB%", Utils.uxCmdStringEscape(cfgFile.getDb()));
		cmd = cmd.replace("%PORT%", cfgFile.getPort()+"");
		return cmd;
	}
}