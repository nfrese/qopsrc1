package at.qop.qoplib.utils;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

public class GeoJsonToHtmlTableWithNested {

    public static String convertGeoJsonToHtmlTable(JsonNode geoJsonNode) {
        if (!geoJsonNode.has("features")) {
            throw new IllegalArgumentException("GeoJSON should contain 'features' array");
        }

        Set<String> columns = new LinkedHashSet<>();
        StringBuilder htmlOutput = new StringBuilder();
        
        JsonNode features = geoJsonNode.get("features");
        for (JsonNode feature : features) {
            JsonNode properties = feature.get("properties");
            collectPropertyKeys(properties, columns, "");
        }

        htmlOutput.append("<html><body>");
        htmlOutput.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse;'>");

        htmlOutput.append("<tr>");
        for (String column : columns) {
            htmlOutput.append("<th>").append(column).append("</th>");
        }
        htmlOutput.append("</tr>");

        for (JsonNode feature : features) {
            htmlOutput.append("<tr>");
            JsonNode properties = feature.get("properties");
            
            for (String column : columns) {
                String[] columnParts = column.split("\\.");
                JsonNode valueNode = getNestedValue(properties, columnParts, 0);
                htmlOutput.append("<td>");
                if (valueNode != null) {
                    if (valueNode.isObject()) {
                        htmlOutput.append(renderPropertiesAsHtmlTable(valueNode));
                    } else if (valueNode.isArray()) {
                        htmlOutput.append(renderArrayAsHtml(valueNode));
                    } else {
                        htmlOutput.append(valueNode.asText());
                    }
                } else {
                    htmlOutput.append("null");
                }
                htmlOutput.append("</td>");
            }
            htmlOutput.append("</tr>");
        }

        htmlOutput.append("</table>");
        htmlOutput.append("</body></html>");
        return htmlOutput.toString();
    }

    private static void collectPropertyKeys(JsonNode properties, Set<String> columns, String prefix) {
        Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode value = field.getValue();

            if (value.isObject()) {
                collectPropertyKeys(value, columns, prefix + key + ".");
            } else {
                columns.add(prefix + key);
            }
        }
    }

    private static JsonNode getNestedValue(JsonNode properties, String[] columnParts, int index) {
        if (properties == null || index >= columnParts.length) {
            return properties;
        }
        String part = columnParts[index];
        if (properties.has(part)) {
            return getNestedValue(properties.get(part), columnParts, index + 1);
        }
        return null;
    }

    private static String renderPropertiesAsHtmlTable(JsonNode propertiesNode) {
        StringBuilder htmlTable = new StringBuilder();
        htmlTable.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse;'>");

        Iterator<Map.Entry<String, JsonNode>> fields = propertiesNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode value = field.getValue();

            htmlTable.append("<tr>");
            htmlTable.append("<td><strong>").append(fieldName).append("</strong></td>");
            htmlTable.append("<td>");
            if (value.isObject()) {
                htmlTable.append(renderPropertiesAsHtmlTable(value)); 
            } else if (value.isArray()) {
                htmlTable.append(renderArrayAsHtml(value));
            } else {
                htmlTable.append(value.asText());
            }
            htmlTable.append("</td>");
            htmlTable.append("</tr>");
        }

        htmlTable.append("</table>");
        return htmlTable.toString();
    }

    private static String renderArrayAsHtml(JsonNode arrayNode) {
        StringBuilder htmlArray = new StringBuilder("<ul>");
        for (JsonNode item : arrayNode) {
            htmlArray.append("<li>").append(item.asText()).append("</li>");
        }
        htmlArray.append("</ul>");
        return htmlArray.toString();
    }

}
