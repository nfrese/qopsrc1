package at.qop.qoplib.batch;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.batch.WriteBatTable.BatRecord;
import at.qop.qoplib.dbconnector.AbstractDbTableReader;
import at.qop.qoplib.dbconnector.DBUtils;
import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.dbconnector.fieldtypes.DbGeometryField;
import at.qop.qoplib.dbconnector.fieldtypes.DbTextField;
import at.qop.qoplib.domains.IGenericDomain;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Profile;

public class BatchCalculation extends BatchCalculationAbstract {

	private static final String geomField = "geom";
	private PerformBatUpdate pbt;
	
	public BatchCalculation(Profile currentProfile) {
		super(currentProfile);
	}

	@Override
	protected void initOutput() {
		pbt = new PerformBatUpdate(currentProfile);
	}
	
	@Override
	protected QuadifyImpl initQuadify() {
		return new QuadifyImpl(maxPerRect, Address.TABLENAME, geomField);
	}
	
	@Override
	protected List<Address> addressesForQuadrant(Envelope envelope) {
		
		String sql = "select * from " + Address.TABLENAME
				+ " WHERE " + geomField 
				+ " && " + DBUtils.stMakeEnvelope(envelope);
		IGenericDomain gd_ = LookupSessionBeans.genericDomain();

		List<Address> addresses = new ArrayList<>();

		
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
				//System.out.println(geom + " - " + name);

				Address currentAddress = new Address();
				currentAddress.geom = (Point)geom;
				currentAddress.name = name;
				addresses.add(currentAddress);
			}

		};
		try {
			gd_.readTable(sql, tableReader);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return addresses;
	}
	
	@Override
	protected void outputRecs(BatRecord[] batRecs) {
		pbt.wbt.insert(batRecs);
	}
	
	@Override
	protected void outputDone() {
		pbt.wbt.done();
	}

	@Override
	protected void failed(Throwable t) {
		t.printStackTrace();
	}

	
}
