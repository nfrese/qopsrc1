package at.qop.qoplib.osmosis;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
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
          for (Tag myTag : myWay.getTags()) {
              if ("amenity".equalsIgnoreCase(myTag.getKey())) {
            	  
            	  ow.print("INSERT INTO osm_pois ");
            	  
            	  
                  System.out.println(" Woha, it's a amenity: " + myWay.getId());
                  break;
              }
          }
        } else if (entityContainer instanceof WayContainer) {
//            Way myWay = ((WayContainer) entityContainer).getEntity();
//            for (Tag myTag : myWay.getTags()) {
//                if ("highway".equalsIgnoreCase(myTag.getKey())) {
//                    System.out.println(" Woha, it's a highway: " + myWay.getId());
//                    break;
//                }
//            }
        } else if (entityContainer instanceof RelationContainer) {
            // Nothing to do here
        } else {
            System.out.println("Unknown Entity!");
        }
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
