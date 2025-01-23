package at.qop.qoplib.osmosis;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import crosby.binary.osmosis.OsmosisReader;
 
public class OsmosisPoisToDb implements Sink {
 
	private ObjectMapper om = new ObjectMapper();
	public final String outputFilename;
	private PrintWriter ow;
	
    public OsmosisPoisToDb(String outputFilename, boolean createTable) {
		super();
		this.outputFilename = outputFilename;
		try {
			this.ow = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFilename), "UTF-8"));
			
			if (createTable) {
				writeDDL();
			}
			this.ow.println("DELETE FROM qop.osm_pois;");
			
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeDDL() {
		String sql = "CREATE TABLE qop.osm_pois (\n"
				+ "	gid serial4 NOT NULL,\n"
				+ "	nodeid bigserial NOT NULL,\n"
				+ "	mainkey text NULL,\n"
				+ "	mainval text NULL,\n"
				+ "	\"name\" text NULL,\n"
				+ "	tags jsonb NULL,\n"
				+ "	geom public.geometry(point, 4326) NULL,\n"
				+ "	CONSTRAINT osm_pois_pkey PRIMARY KEY (gid)\n"
				+ ");\n"
				+ "CREATE INDEX osm_pois_geom_gist ON qop.osm_pois USING gist (geom);";
		
		ow.println(sql);
		
	}

	@Override
    public void initialize(Map<String, Object> arg0) {
    }
 
    @Override
    public void process(EntityContainer entityContainer) {
        if (entityContainer instanceof NodeContainer) {
          Node n = ((NodeContainer) entityContainer).getEntity();
          String mainKey = null;
          for (Tag myTag : n.getTags()) {
              if ("amenity".equalsIgnoreCase(myTag.getKey())) {
            	  mainKey = "amenity";
                  break;
              } else  if ("office".equalsIgnoreCase(myTag.getKey())) {
            	  mainKey = "office";
                  break;
              }
          }
          
          Map<String,String> tagsMap = tagsMap(n.getTags());
          
          if (mainKey != null) {
        	  String mainValue = tagsMap.get("amenity");
        	  String json = tagsToJson(tagsMap);
        	  ow.print("INSERT INTO qop.osm_pois ");
        	  ow.print("(nodeid, mainkey, mainval, \"name\", tags, geom)");
        	  ow.print(" VALUES (");
        	  ow.print(n.getId() + ", ");
        	  ow.print(writeStr(mainKey)+ ", ");
        	  ow.print(writeStr(mainValue)+ ", ");
        	  ow.print(writeStr(tagsMap.get("name"))+ ", ");
        	  ow.print(writeStrS(json )+ "::jsonb, ");
        	  ow.print("ST_GeomFromText('" + geom(n) + "')");
        	  ow.println(");");
          }
        } else if (entityContainer instanceof WayContainer) {
        } else if (entityContainer instanceof RelationContainer) {
        } else {
            System.out.println("Unknown Entity!");
        }
    }
 
    private Map<String, String> tagsMap(Collection<Tag> tags) {
    	Map<String, String> m = new LinkedHashMap<>();
    	for (Tag tag:tags)
    	{
    		m.put(tag.getKey(), tag.getValue());
    	}
		return m;
	}

	private String geom(Node n) {
		return "POINT(" + n.getLongitude() + " " + n.getLatitude() + ")";
	}

	private String writeStr(String s) {
		if (s == null) return null;
		return "'" + s.replace("'", "''").replace("\n", "\\n") + "'";
	}
	
	private String writeStrS(String s) {
		if (s == null) return null;
		return "'" + s.replace("'", "''").replace("\n", "\\n") + "'";
	}

	private String tagsToJson(Map<String, String> tagsMap) {
		ObjectNode on = om.createObjectNode();
		for (Entry<String, String> e : tagsMap.entrySet())
		{
			on.put(e.getKey(), e.getValue());
		}
		return on.toString();
	}

	@Override
    public void complete() {
    }
 
    @Override
    public void close() {
    	ow.flush();
    }
 
    public static void importAmenitys(String filename, String outputfilename, boolean createTable ) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(filename);
        OsmosisReader reader = new OsmosisReader(inputStream);
        reader.setSink(new OsmosisPoisToDb(outputfilename, createTable));
        reader.run();
    }

}
