package at.qop.qoplib.osmosis;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.testcontainers.shaded.org.apache.commons.lang.StringEscapeUtils;

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
			this.ow.println("DELETE FROM qop.pvs_osm_poi;");
			
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeDDL() {
		String sql = ""
				+ "-- DROP TABLE IF EXISTS qop.pvs_osm_poi;\n"
				+ "-- CREATE TABLE qop.pvs_osm_poi (\n"
				+ "-- 	gid serial4 NOT NULL,\n"
				+ "-- 	nodeid bigserial NOT NULL,\n"
				+ "--	mainkey text NULL,\n"
				+ "--	mainval text NULL,\n"
				+ "--	\"name\" text NULL,\n"
				+ "--	tags jsonb NULL,\n"
				+ "--	geom public.geometry(point, 4326) NULL,\n"
				+ "--	CONSTRAINT pvs_osm_poi_pkey PRIMARY KEY (gid)\n"
				+ "-- );\n"
				+ "-- CREATE INDEX pvs_osm_poi_geom_gist ON qop.pvs_osm_poi USING gist (geom);\n";
		
		ow.println(sql);
		
	}

	@Override
    public void initialize(Map<String, Object> arg0) {
    }
 
	private Map<Long,Way> collectedWays = new HashMap<>();
	public int pass = 1;
	
    @Override
    public void process(EntityContainer entityContainer) {
    	if (pass == 1) {
    		processStep1(entityContainer);
    	}
    	else if (pass == 2)
    	{
    		processStep2(entityContainer);
    	}
    	else
    	{
    		throw new RuntimeException("already finished!");
    	}
    	
    }   	
    
    public void processStep1(EntityContainer entityContainer) {
    	
        if (entityContainer instanceof WayContainer) {
          Way w = ((WayContainer) entityContainer).getEntity();
          String mainKey = checkType(w);
          if (mainKey != null) {
    		Iterator<WayNode> it = w.getWayNodes().iterator();
    		if (it.hasNext()) {
    			WayNode wn = it.next();
    			collectedWays.put(wn.getNodeId(), w);
    		}
          }
        }
    }
    	
    public void processStep2(EntityContainer entityContainer) {
    	
        if (entityContainer instanceof NodeContainer) {
          Node n = ((NodeContainer) entityContainer).getEntity();
          Way w = collectedWays.get(n.getId());
          if (w != null)
          {
          	  String mainKey = checkType(w);

        	  Map<String,String> tagsMap = tagsMap(w.getTags());
        	  if (mainKey != null) {
        		  String mainValue = tagsMap.get(mainKey);
        		  writeInsert(n, mainKey, tagsMap, mainValue);
        	  }
          }
          else 
          {
        	  String mainKey = checkType(n);

        	  Map<String,String> tagsMap = tagsMap(n.getTags());
        	  if (mainKey != null) {
        		  String mainValue = tagsMap.get(mainKey);
        		  writeInsert(n, mainKey, tagsMap, mainValue);
        	  }
          }
        } else if (entityContainer instanceof WayContainer) {
        } else if (entityContainer instanceof RelationContainer) {
        } else {
            System.out.println("Unknown Entity: " + entityContainer);
        }
    }

	private void writeInsert(Node n, String mainKey, Map<String, String> tagsMap, String mainValue) {
		String json = tagsToJson(tagsMap);
		  ow.print("INSERT INTO qop.pvs_osm_poi ");
		  ow.print("(nodeid, mainkey, mainval, \"name\", tags, geom)");
		  ow.print(" VALUES (");
		  ow.print(n.getId() + ", ");
		  ow.print(writeStr(mainKey)+ ", ");
		  ow.print(writeStr(mainValue)+ ", ");
		  ow.print(writeStr(tagsMap.get("name"))+ ", ");
		  ow.print(writeStr(json )+ "::jsonb, ");
		  ow.print("ST_GeomFromText('" + geom(n) + "')");
		  ow.println(");");
	}

	private String checkType(Entity n) {
		String mainKey = null;
		for (Tag myTag : n.getTags()) {
			if ("amenity".equalsIgnoreCase(myTag.getKey())) {
				mainKey = "amenity";
				break;
			} else  if ("office".equalsIgnoreCase(myTag.getKey())) {
				mainKey = "office";
				break;
			} else  if ("shop".equalsIgnoreCase(myTag.getKey())) {
				mainKey = "shop";
				break;
			}
		}
		return mainKey;
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
		return "E'" + StringEscapeUtils.escapeJavaScript(s) + "'";
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
        OsmosisPoisToDb sink = new OsmosisPoisToDb(outputfilename, createTable);
        {
        	InputStream inputStream = new FileInputStream(filename);
        	OsmosisReader reader = new OsmosisReader(inputStream);
        	reader.setSink(sink);
        	reader.run();
        }
        sink.pass = 2;
        {
        	InputStream inputStream = new FileInputStream(filename);
        	OsmosisReader reader = new OsmosisReader(inputStream);
        	reader.setSink(sink);
        	reader.run();
        }
        
    }

}
