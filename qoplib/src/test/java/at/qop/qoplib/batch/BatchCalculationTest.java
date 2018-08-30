package at.qop.qoplib.batch;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import at.qop.qoplib.batch.WriteBatTable.BatRecord;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Profile;

public class BatchCalculationTest {
	
	@Test
	public void test() throws ParseException
	{
		List<Address> input = new ArrayList<>();
		
		{
			Address adr = new Address();
			adr.name = "Rauhensteingasse 10";
			adr.geom = (Point) new WKTReader().read("POINT (16.3724265546418 48.2061121370655)");
			input.add(adr);
			
		}
		{
			Address adr = new Address();
			adr.name = "Albertinaplatz 2";
			adr.geom = (Point) new WKTReader().read("POINT (16.3695610097329 48.2042327131692)");
			input.add(adr);
		}
		{
			Address adr = new Address();
			adr.name = "Neuer Markt 8";
			adr.geom = (Point) new WKTReader().read("POINT (16.3704315013128 48.2053297142949)");
			input.add(adr);
		}
		{
			Address adr = new Address();
			adr.name = "Plankengasse 3";
			adr.geom = (Point) new WKTReader().read("POINT (16.3699659807455 48.2063095697309)");
			input.add(adr);
		}
		{
			Address adr = new Address();
			adr.name = "Liebenberggasse 7/3";
			adr.geom = (Point) new WKTReader().read("POINT (16.3784976304613 48.2060303886915)");
			input.add(adr);
		}
		{
			Address adr = new Address();
			adr.name = "Liebenberggasse 7/2";
			adr.geom = (Point) new WKTReader().read("POINT (16.3782822940926 48.2059552422407)");
			input.add(adr);
		}
		{
			Address adr = new Address();
			adr.name = "Liebenberggasse 7/1";
			adr.geom = (Point) new WKTReader().read("POINT (16.3784724170685 48.2058753275844)");
			input.add(adr);
		}

		Profile currentProfile = new Profile();
		BatchCalculationInMemory bc = new BatchCalculationInMemory(currentProfile, input);
		
		bc.run();
		
		List<BatRecord> output = bc.getOutput();
		
		System.out.println(output);
		
	}

}
