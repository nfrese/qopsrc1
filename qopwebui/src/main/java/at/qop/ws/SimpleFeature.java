package at.qop.ws;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.vividsolutions.jts.geom.Geometry;

public class SimpleFeature {
	public String type="Feature";
	public String id;
	public Map<String,Object> properties = new LinkedHashMap<>();
	public JsonNode geometry;
	public transient Geometry geom_;
	
	public boolean containsText(String textFilter) {
		if (textFilter == null || textFilter.isEmpty())
		{
			return true; // no filtering
		}
		else
		{
			return f(properties.values(), textFilter);
		}
	}

	private static boolean f(Collection<?> collection, String textFilter) {
		for (Object p : collection) {
			if (p instanceof Map) {
				boolean fnd = f(((Map<?,?>)p).values(), textFilter);
				if (fnd) {
					return true;
				}
			} else
			if (StringUtils.containsIgnoreCase(String.valueOf(p),textFilter)) {
				return true;
			}
		}
		return false;
	}

	public SimpleFeature cloneIt() {
		SimpleFeature clone = new SimpleFeature();
		clone.id = id;
		clone.type = type;
		clone.geom_ = geom_;
		clone.geometry = geometry != null ? geometry.deepCopy() : null;
		clone.properties.putAll(properties);
		return clone;
	}
}