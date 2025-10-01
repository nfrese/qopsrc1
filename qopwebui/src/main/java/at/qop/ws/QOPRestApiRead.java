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
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

import javax.servlet.ServletException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import at.qop.qoplib.Config;

@RestController
public class QOPRestApiRead extends QOPRestApiBase {
       
    public QOPRestApiRead() {
        super();
    }

    public static class RoutingResults {
		private static final double THRES = 15.;

		public TT walk = new TT();

		public TT bike = new TT();

		public TT eBike = new TT();
		
		public TT publicTransport = new TT();
		
		public TT car = new TT();

		public void set() {
			
			eBike.minutes  = bike.minutes / 1.5;
			publicTransport.minutes = car.minutes * 2;
			publicTransport.minutes = publicTransport.minutes > 7 ? publicTransport.minutes : Double.NaN;

			walk.display = walk.minutes <= THRES;
			bike.display = bike.minutes <= THRES;
			eBike.display = eBike.minutes <= THRES;
			publicTransport.display = publicTransport.minutes <= THRES;
		}

		public boolean disp() {
			return walk.display || bike.display  || eBike.display || publicTransport.display || car.display;
		}
    	
    }
    
    public static class TT {
		public double minutes;
		public boolean display;    	
    }
    
    @GetMapping("/qop/rest/api/read")
	protected ResponseEntity<?> read(
			@RequestParam(name="username") String username, 
			@RequestParam(name="password") String password, 
			@RequestParam(name="table") String table,
			@RequestParam(name="maxFeatures", defaultValue = "10000") Integer maxFeatures,
			@RequestParam(name="format", required = false, defaultValue = "geojson") String format
		) throws ServletException, IOException, SQLException {
		
		@SuppressWarnings("unused")
		Config cfg = checkAuth(username, password);
		
		String sql = "SELECT * FROM " + table;
		if (maxFeatures != null)
		{
			sql += " LIMIT " + maxFeatures;
		}
		
		List<SimpleFeature> outFeatures = readInt(table, sql);
		return returnGeoJsonOrHtml(outFeatures, format);
	}

    @GetMapping("/qop/rest/api/readsingle")
	protected ResponseEntity<?> single(
			@RequestParam(name="username") String username, 
			@RequestParam(name="password") String password, 
			@RequestParam(name="id") String id
		) throws ServletException, IOException, SQLException {
		
		@SuppressWarnings("unused")
		Config cfg = checkAuth(username, password);
		
		String[] split = id.split(":");
		String tableName;
		if (split.length > 1)
		{
			tableName = split[0];
		}
		else
		{
			throw new RuntimeException("invalid id, no ':' to separate tablename " + id);
		}
		
		String sql = "SELECT * FROM " + tableName + " WHERE fid=" + escSqlStr(id)+ "";
		
		List<SimpleFeature> outFeatures = readInt(tableName, sql);
		if (outFeatures.size() == 1)
		{
			return returnJson(outFeatures.get(0));
		}
		else
		{
			return returnJson(null);
		}
	}
    
    @GetMapping("/qop/rest/api/decode64")
	protected ResponseEntity<byte[]> decode64(@RequestParam String dataUrl) {
    	String[] sp1 = dataUrl.split(";base64,");
    	if (sp1.length != 2)
    	{
    		throw new RuntimeException("no ',' found");
    	}
    	String[] sp2 = sp1[0].split(":");
       	if (sp1.length != 2)
    	{
    		throw new RuntimeException("no ':' found in first part");
    	}
       	String mime = sp2[1];
       	return ResponseEntity.ok().header("Content-Type", mime).body(Base64.getDecoder().decode(sp1[1]));
    }

    
}
