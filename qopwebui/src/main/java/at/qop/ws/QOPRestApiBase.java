package at.qop.ws;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.qop.qoplib.Config;

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

	protected ResponseEntity<?> returnGeoJson(List<? extends SimpleFeature> outFeatures) throws JsonProcessingException {
		Map<String,Object> outRoot = new LinkedHashMap<>();
		outRoot.put("features", outFeatures);
		String jsonOut = om().writeValueAsString(outRoot);

		return ResponseEntity.ok().header("Content-Type", "application/json;charset=UTF-8").body(jsonOut);
	}

	protected String escSqlStr(String sql) {
		return "'" + sql.replace("'", "''") + "'";
	}
}
