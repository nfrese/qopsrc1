package at.qop.qoplib.osmosis;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.io.output.DeferredFileOutputStream;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
 
import crosby.binary.osmosis.OsmosisReader;
 
/**
 * Receives data from the Osmosis pipeline and prints ways which have the
 * 'highway key.
 * 
 * @author pa5cal
 */
public class Mysink implements Sink {
 
	public final String outputFilename;
	private PrintWriter ow;
	
    public Mysink(String outputFilename) {
		super();
		this.outputFilename = outputFilename;
		try {
			this.ow = new PrintWriter(new FileOutputStream(outputFilename));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
    public void initialize(Map<String, Object> arg0) {
    }
 
    @Override
    public void process(EntityContainer entityContainer) {
        if (entityContainer instanceof NodeContainer) {
          Node myWay = ((NodeContainer) entityContainer).getEntity();
          boolean write = false;
          for (Tag myTag : myWay.getTags()) {
              if ("amenity".equalsIgnoreCase(myTag.getKey())) {
            	  write = true;
                  break;
              }
          }
          
          Map<String,String> tagsMap = tagsMap(myWay.getTags());
          
          if (write) {
        	  String mainKey = "amenity";
        	  String mainValue = tagsMap.get("amenity");
        	  String json = tagsToJson(tagsMap);
        	  ow.print("INSERT INTO osm_pois ");
        	  ow.print("(nodeid, mainkey, mainval, \"name\", tags, geom)");
        	  ow.print(" VALUES (");
        	  ow.print(myWay.getId() + ", ");
        	  ow.print(writeStr(tagsMap.get(mainKey)));
        	  ow.print(writeStr(tagsMap.get(mainValue)));
        	  ow.print(writeStr(tagsMap.get("name")));
        	  ow.print(writeStr(json));
        	  ow.print("ST_FromText(" + geom(myWay) + ")");
        	  ow.println(")");
          }
        } else if (entityContainer instanceof WayContainer) {
        } else if (entityContainer instanceof RelationContainer) {
        } else {
            System.out.println("Unknown Entity!");
        }
    }
 
    private Map<String, String> tagsMap(Collection<Tag> tags) {
		// TODO Auto-generated method stub
		return null;
	}

	private String geom(Node myWay) {
		// TODO Auto-generated method stub
		return null;
	}

	private char[] writeStr(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	private String tagsToJson(Map<String, String> tagsMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public void complete() {
    }
 
    @Override
    public void close() {
    }
 
    public static void importAmenitys(String filename, String outputfilename) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(filename);
        OsmosisReader reader = new OsmosisReader(inputStream);
        reader.setSink(new Mysink(outputfilename));
        reader.run();
    }

}
