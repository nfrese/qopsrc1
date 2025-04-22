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

package at.qop.ws;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.openstreetmap.osmosis.core.Osmosis;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import at.qop.qoplib.osmosis.OsmosisPoisToDb;

@RestController
public class QOPRestApiUpdatePois extends QOPRestApiBase {
       
    public QOPRestApiUpdatePois() {
        super();
    }
    
    @GetMapping("/qop/rest/api/pois/update")
	protected ResponseEntity<String> updatePois() throws MalformedURLException, IOException {
    	
    	String pbfUrl = System.getenv("QOP_COMPLETE_PBF_URL"); // "https://download.geofabrik.de/europe/austria-latest.osm.pbf";
    	
    	
		String localPbfPath = System.getenv("QOP_COMPLETE_PBF_PATH");
		String localREducedPolyPath = System.getenv("QOP_REDUCE_POLY_PATH");
		String localREducedPbfPath = System.getenv("QOP_REDUCED_PBF_PATH");
		
		InputStream in = new URL(pbfUrl).openStream();
		Files.copy(in, Paths.get(localPbfPath), StandardCopyOption.REPLACE_EXISTING);
		
		
		Osmosis.main(new String[]{
				"--read-pbf", 
				localPbfPath, 
				"--bounding-polygon", 
				"completeWays=yes", "file=" + localREducedPolyPath,
				"--write-pbf", 
				localREducedPbfPath});

		
		String qopWorkingDir = System.getenv("QOP_WORKING_DIR");
		OsmosisPoisToDb.importAmenitys(localPbfPath , qopWorkingDir + "/pois.sql", true);

    	
    	// TODO
       	return ResponseEntity.ok().header("Content-Type", "application/json").body("{}");
    }

    
}
