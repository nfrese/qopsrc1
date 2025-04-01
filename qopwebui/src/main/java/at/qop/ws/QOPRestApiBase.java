package at.qop.ws;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.geojson.GeoJsonWriter;

import at.qop.qoplib.Config;
import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.DbTableReader;
import at.qop.qoplib.dbconnector.fieldtypes.DbTextField;

public abstract class QOPRestApiBase {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	protected Config checkAuth(String username, String password) {
		if (username == null) throw new RuntimeException("URL parameter username required");
		if (password == null) throw new RuntimeException("URL parameter password required");

		Config cfg = Config.read();
		if (!password.equals(cfg.getUserPassword(username)))
		{
			throw new RuntimeException("Invalid username/password!");
		}
		return cfg;
	}

	protected ObjectMapper om() {
		return OBJECT_MAPPER;
	}

	protected ResponseEntity<?> returnGeoJson(List<? extends SimpleFeature> outFeatures) throws JsonProcessingException 
	{
		return returnGeoJson(outFeatures, null);
	}	
	
	protected ResponseEntity<?> returnGeoJson(List<? extends SimpleFeature> outFeatures, Map<String,Object> extended) throws JsonProcessingException {
		Map<String,Object> outRoot = new LinkedHashMap<>();
		outRoot.put("type","FeatureCollection");
		outRoot.put("features", outFeatures);
		if (extended != null)
		{
			outRoot.put("extended", extended);
		}
		return returnJson(outRoot);
	}

	protected ResponseEntity<?> returnJson(Object outRoot) throws JsonProcessingException {
		String jsonOut = om().writeValueAsString(outRoot);

		return ResponseEntity.ok()
				.header("Content-Type", "application/json;charset=UTF-8")
				.header("Access-Control-Allow-Origin", "*")
				.body(jsonOut);
	}

	protected String escSqlStr(String sql) {
		return "'" + sql.replace("'", "''") + "'";
	}
	
	protected List<SimpleFeature> readInt(String table, String sql)
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

}
