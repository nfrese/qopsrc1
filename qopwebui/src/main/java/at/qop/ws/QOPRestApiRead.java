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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.geojson.GeoJsonWriter;

import at.qop.qoplib.Config;
import at.qop.qoplib.Constants;
import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.calculation.CRSTransform;
import at.qop.qoplib.calculation.IRouter;
import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.DbTableReader;
import at.qop.qoplib.dbconnector.fieldtypes.DbInt4Field;
import at.qop.qoplib.dbconnector.fieldtypes.DbTextField;
import at.qop.qoplib.entities.ModeEnum;
import at.qop.qoplib.osrmclient.LonLat;
import at.qop.qoplib.osrmclient.OSRMClient;

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
			@RequestParam(name="maxFeatures", defaultValue = "10000") Integer maxFeatures
		) throws ServletException, IOException, SQLException {
		
		Config cfg = checkAuth(username, password);
		
		String sql = "SELECT * FROM " + table;
		if (maxFeatures != null)
		{
			sql += " LIMIT " + maxFeatures;
		}
		
		List<SimpleFeature> outFeatures = readInt(table, sql);
		return returnGeoJson(outFeatures);
	}

	private List<SimpleFeature> readInt(String table, String sql)
			throws SQLException, JsonProcessingException, JsonMappingException {
		String geomField ="geom";
		
		List<SimpleFeature> outFeatures = new ArrayList<>();

			DbTableReader reader = new DbTableReader();
			
			LookupSessionBeans.genericDomain().readTable(
					sql, reader );
			int cnt=0;
			
			DbTextField fidField = reader.table.textField("fid");
			
			for (DbRecord record : reader.records)
			{
				SimpleFeature outFeature = new SimpleFeature();
				if (fidField != null)
				{
					outFeature.id = fidField.get(record);
				}
				else
				{
					outFeature.id = table + ":rec_"+ cnt;
				}
				
				for (int i = 0; i < reader.table.colNames.length;i++) {
					String colName = reader.table.colNames[i];
					if (reader.table.typeNames[i].equals("geometry"))
					{
						Geometry value = reader.table.geometryField(colName).get(record);
						GeoJsonWriter gw = new GeoJsonWriter();
						String json = gw.write(value);
						JsonNode jo = om().readTree(json);
						if (geomField.equals(colName))
						{
							outFeature.geometry= jo;
						}
						else
						{
							outFeature.properties.put(colName, jo);
						}
					}
					else if (reader.table.typeNames[i].equals("jsonb"))
					{
						String json = String.valueOf(record.values[i]);
						outFeature.properties.put(colName, om().readTree(json));
					}
					else
					{
						Object value = record.values[i];
						outFeature.properties.put(colName, value);
					}
				}
				outFeatures.add(outFeature);
				cnt++;
			}
		return outFeatures;
	}

    @GetMapping("/qop/rest/api/readsingle")
	protected ResponseEntity<?> single(
			@RequestParam(name="username") String username, 
			@RequestParam(name="password") String password, 
			@RequestParam(name="id") String id
		) throws ServletException, IOException, SQLException {
		
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

    
}
