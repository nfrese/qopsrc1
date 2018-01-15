package at.qop.qoplib.batch;

import java.sql.SQLException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.ConfigFile;
import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.calculation.Calculation;
import at.qop.qoplib.calculation.DbLayerSource;
import at.qop.qoplib.calculation.IRouter;
import at.qop.qoplib.calculation.LayerSource;
import at.qop.qoplib.dbconnector.AbstractDbTableReader;
import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.dbconnector.fieldtypes.DbGeometryField;
import at.qop.qoplib.dbconnector.fieldtypes.DbTextField;
import at.qop.qoplib.domains.IGenericDomain;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.osrmclient.OSRMClient;

public class BatchCalculation {
	
	Profile currentProfile;
	LayerSource source;
	ConfigFile cf;
	IRouter router;
	int count=0;
	
	public BatchCalculation(Profile currentProfile) {
		this.currentProfile = currentProfile;
		
		source = new DbLayerSource();
		cf = ConfigFile.read();
		router = new OSRMClient(cf.getOSRMHost(), cf.getOSRMPort());
	}

	public void run()
	{
		String sql = "select * from " + Address.TABLENAME;
		IGenericDomain gd_ = LookupSessionBeans.genericDomain();
		
		AbstractDbTableReader tableReader = new AbstractDbTableReader() {

			DbTable table;
			private DbGeometryField geomField;
			private DbTextField nameField;
			
			@Override
			public void metadata(DbTable table) {
				this.table = table;
				geomField = table.geometryField("geom");
				nameField = table.textField("name");
			}

			@Override
			public void record(DbRecord record) {
				
				Geometry geom = geomField.get(record);
				String name = nameField.get(record);
				System.out.println(geom + " - " + name);
				
				Address currentAddress = new Address();
				currentAddress.geom = (Point)geom;
				currentAddress.name = name;
				
				Calculation calculation = new Calculation(currentProfile, currentAddress, source, router);
				calculation.run();
				System.out.println("PROGRESS:" + count);
				count++;
			}
			
		};
		try {
			gd_.readTable(sql, tableReader);

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}		
		
		
	}

}
