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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.Osmosis;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import at.qop.qoplib.dbconnector.DBUtils;
import at.qop.qoplib.osmosis.OsmosisPoisToDb;

@RestController
public class QOPRestApiUpdatePois extends QOPRestApiBase {
       
    public QOPRestApiUpdatePois() {
        super();
    }
    
    @GetMapping("/qop/rest/api/osmpois/update")
	protected ResponseEntity<String> updatePois(
			@RequestParam(name="username") String username, 
			@RequestParam(name="password") String password
			) throws MalformedURLException, IOException {
    	
    	checkAuth(username, password);
    	
    	String pbfUrl = System.getenv("QOP_PVS_COMPLETE_PBF_URL"); // "https://download.geofabrik.de/europe/austria-latest.osm.pbf";
    	
    	String qopWorkingDir = System.getenv("QOP_PVS_WORKING_DIR");
		String localPbfPath = qopWorkingDir + System.getenv("QOP_PVS_LOCAL_PBF_FILENAME");
		String localREducedPolyPath = System.getenv("QOP_PVS_REDUCE_POLY_PATH");
		String localREducedPbfPath = qopWorkingDir + System.getenv("QOP_PVS_REDUCED_PBF_FILENAME");
		
		 
		System.out.println("1) downloading " + pbfUrl + " to " + localPbfPath);
		
		InputStream in = new URL(pbfUrl).openStream();
		Files.copy(in, Paths.get(localPbfPath), StandardCopyOption.REPLACE_EXISTING);
		
		System.out.println("2) extracting " + localREducedPbfPath);
		
		Osmosis.run(new String[]{
				"--read-pbf", 
				localPbfPath, 
				"--bounding-polygon", 
				"completeWays=yes", "file=" + localREducedPolyPath,
				"--write-pbf", 
				localREducedPbfPath});

		
		String sqlScriptFilename = qopWorkingDir + "/pois.sql";
		System.out.println("3) creating import sql script " + sqlScriptFilename);
		
		OsmosisPoisToDb.importAmenitys(localPbfPath , sqlScriptFilename, true);
		
		System.out.println("4) applying sql script " + sqlScriptFilename);

		try {
			OsmosisPoisToDb.importScript2DB(sqlScriptFilename);
		} catch (ClassNotFoundException | SQLException e) {
			throw new RuntimeException(e);
		}
		
       	return ResponseEntity.ok().header("Content-Type", "application/json").body("{ \"updateFinisheD\": true }");
    }
    

    
}
