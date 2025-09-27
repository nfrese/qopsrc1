package at.qop.qoplib.osmosis;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.text.StringEscapeUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.openstreetmap.osmosis.core.Osmosis;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import at.qop.qoplib.dbconnector.DBUtils;
import crosby.binary.osmosis.OsmosisReader;
 
public class OsmosisPoisToDb implements Sink {
 
	public List<String> filter = new ArrayList<>();
	{
		filter.add("amenity=*");
		filter.add("office=*");
		filter.add("shop=*");
		filter.add("leisure=*");
		filter.add("highway=footway");	
		filter.add("natural=tree");
		filter.add("bridge=yes");
		filter.add("historic=castle");
		filter.add("historic=memorial");
		filter.add("historic=wayside_shrine");
		filter.add("landuse=cemetery");
		filter.add("water=lake");
		filter.add("water=pond");
		filter.add("landuse=vineyard");
		filter.add("landuse=orchard");
		filter.add("natural=scrub");
		filter.add("landuse=forest");
		filter.add("tourism=artwork");
		filter.add("tourism=guest_house");
		filter.add("emergency=ambulance_station");
		filter.add("healthcare=rehabilitation");
		
		filter.add("cuisine=buschenschank");
		filter.add("healthcare=nurse");
		filter.add("healthcare=centre");
		filter.add("public_transport=stop_position");
		filter.add("public_transport=platform");
		filter.add("building=civic");
		filter.add("tourism=museum");
		filter.add("tourism=apartment");
		filter.add("tourism=chalet");
		filter.add("tourism=hostel");
		filter.add("tourism=hotel");
		filter.add("tourism=motel");
		filter.add("tourism=theme_park");
		filter.add("tourism=gallery");
		filter.add("tourism=picnic_site");
		filter.add("tourism=information");
		filter.add("drink:wine=retail");

	}
	
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
			this.ow.println("INSERT INTO qop.pvs_updatelog "
					+ "(what, timestamp) "
					+ "VALUES ('osmpois', now())");
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
 
	private static GeometryFactory gf = new GeometryFactory();
	
	public static class WayKeep {
		Way way;
		List<Coordinate> shell = new ArrayList<Coordinate>();

		public void addNode(Node n) {
			shell.add(new Coordinate(n.getLongitude(), n.getLatitude()));
		}
		
		public Coordinate getCentroid() {
			if (shell.size() < 1)
			{
				throw new RuntimeException("no coords for way " + way);
			}
			else if (shell.size() < 2)
			{
				return shell.get(0);
			}
			else if (shell.size() < 3)
			{
				LineString ls = gf.createLineString(shell.toArray(new Coordinate[shell.size()]));
				return ls.getCentroid().getCoordinate();
			}
			else
			{
				shell.add(shell.get(0));
				Polygon poly = gf.createPolygon(shell.toArray(new Coordinate[shell.size()]));
				return poly.getCentroid().getCoordinate();
			}
		}
		
	}
	
	private Map<Long,WayKeep> collectedWays = new HashMap<>();
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
  			WayKeep wk = new WayKeep();
  			wk.way = w;
  			
    		Iterator<WayNode> it = w.getWayNodes().iterator();
    		while (it.hasNext()) {
    			WayNode wn = it.next();
    			
    			collectedWays.put(wn.getNodeId(), wk);
    		}
          }
        }
    }
    	
    public void processStep2(EntityContainer entityContainer) {
    	
        if (entityContainer instanceof NodeContainer) {
          Node n = ((NodeContainer) entityContainer).getEntity();
          WayKeep w = collectedWays.get(n.getId());
          if (w != null)
          {
        	  w.addNode(n);
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

    public void postProcess() 
    {
    	for (WayKeep wk : new HashSet<WayKeep>(collectedWays.values()))
    	{
    		String mainKey = checkType(wk.way);

    		Map<String,String> tagsMap = tagsMap(wk.way.getTags());
    		if (mainKey != null) {
    			String mainValue = tagsMap.get(mainKey);
    			writeInsert(wk, mainKey, tagsMap, mainValue);
    		}
    	}
    	ow.flush();
    }
    
	private void writeInsert(Node n, String mainKey, Map<String, String> tagsMap, String mainValue) {
		String json = tagsToJson(tagsMap);
		  ow.print("INSERT INTO qop.pvs_osm_poi ");
		  ow.print("(nodeid, mainkey, mainval, \"name\", tags, geom, osm_element)");
		  ow.print(" VALUES (");
		  ow.print(n.getId() + ", ");
		  ow.print(writeStr(mainKey)+ ", ");
		  ow.print(writeStr(mainValue)+ ", ");
		  ow.print(writeStr(tagsMap.get("name"))+ ", ");
		  ow.print(writeStr(json )+ "::jsonb, ");
		  ow.print("ST_GeomFromText('" + geom(n) + "'), ");
		  ow.print("'osm:node'");
		  ow.println(");");
	}
	
	private void writeInsert(WayKeep wk, String mainKey, Map<String, String> tagsMap, String mainValue) {
		String json = tagsToJson(tagsMap);
		  ow.print("INSERT INTO qop.pvs_osm_poi ");
		  ow.print("(nodeid, mainkey, mainval, \"name\", tags, geom, osm_element)");
		  ow.print(" VALUES (");
		  ow.print(wk.way.getId() + ", ");
		  ow.print(writeStr(mainKey)+ ", ");
		  ow.print(writeStr(mainValue)+ ", ");
		  ow.print(writeStr(tagsMap.get("name"))+ ", ");
		  ow.print(writeStr(json )+ "::jsonb, ");
		  ow.print("ST_GeomFromText('" + geom(wk) + "')"+ ", ");
		  ow.print("'osm:way'");
		  ow.println(");");
	}

	private String checkType(Entity n) {
		String mainKey = null;
		for (Tag myTag : n.getTags()) {
			for (String f :filter) {
				String[] s = f.split("=");
				String key = s[0];
				String value = s[1];
				
				if (key.equalsIgnoreCase(myTag.getKey())) {
					if (value.equals("*") || value.equals(myTag.getValue())) {
						mainKey = key;
						break;
					}
				}
				
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
	
	private String geom(WayKeep wk) {
		Coordinate centroid = wk.getCentroid();
		return "POINT(" +centroid.x + " " + centroid.y + ")";
	}

	private String writeStr(String s) {
		if (s == null) return null;
		return "E'" + StringEscapeUtils.escapeEcmaScript(s) + "'";
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
        	System.out.println("pass " + sink.pass);
        	InputStream inputStream = new FileInputStream(filename);
        	OsmosisReader reader = new OsmosisReader(inputStream);
        	reader.setSink(sink);
        	reader.run();
        }
        sink.pass = 2;
        {
        	System.out.println("pass " + sink.pass);
        	InputStream inputStream = new FileInputStream(filename);
        	OsmosisReader reader = new OsmosisReader(inputStream);
        	reader.setSink(sink);
        	reader.run();
        }
        System.out.println("postprocess");
        sink.postProcess();
        
    }
    
	public static void importScript2DB(String sqlScriptFilename) throws ClassNotFoundException, SQLException {
		String jdbcUrl = "jdbc:postgresql://" +  System.getenv("QOP_DBHOST") + ":5432/" + System.getenv("QOP_DB");
		String username = System.getenv("QOP_DBUSER");
		String password = System.getenv("QOP_DBPASSWD");

		System.out.println("connecting to " + jdbcUrl + " with user " + username);

		Class.forName("org.postgresql.Driver");

		Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

		DBUtils.importBatchScript(connection, sqlScriptFilename, 10000);
		connection.close();
	}
	
	public static void importAll(String pbfUrl, String qopWorkingDir, String localPbfPath, String localREducedPolyPath,
			String localREducedPbfPath) throws IOException, MalformedURLException, FileNotFoundException {
		System.out.println("1) downloading " + pbfUrl + " to " + localPbfPath);
				
		RedirectAwareDownloader.download(pbfUrl, localPbfPath);
		
		System.out.println("2) extracting " + localREducedPbfPath + " with bounding polygon " + localREducedPolyPath);
		
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
	}

}
