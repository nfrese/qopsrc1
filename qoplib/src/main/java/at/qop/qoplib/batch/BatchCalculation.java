/* 
 * Copyright (C) 2018 Norbert Frese
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
*/

package at.qop.qoplib.batch;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import at.qop.qoplib.Config;
import at.qop.qoplib.Constants;
import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.batch.WriteBatTable.BatRecord;
import at.qop.qoplib.calculation.DbLayerSource;
import at.qop.qoplib.dbconnector.AbstractDbTableReader;
import at.qop.qoplib.dbconnector.DBUtils;
import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.dbconnector.fieldtypes.DbGeometryField;
import at.qop.qoplib.dbconnector.fieldtypes.DbTextField;
import at.qop.qoplib.domains.IGenericDomain;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.osrmclient.OSRMClient;

public class BatchCalculation extends BatchCalculationAbstract {

	private final String TABLENAME; // Address.TABLENAME;
	private final String geomFieldName; // "geom";
	private final String nameFieldName; // "name";
	private PerformBatUpdate pbt;
	
	public BatchCalculation(Profile currentProfile, String pointTableName, String geomFieldName, String nameFieldName) {
		super(currentProfile);
		this.TABLENAME = pointTableName;
		this.geomFieldName = geomFieldName;
		this.nameFieldName = nameFieldName;
	}

	@Override
	protected OSRMClient initRouter() {
		Config cf = Config.read();
		return new OSRMClient(cf.getOSRMConf(), Constants.SPLIT_DESTINATIONS_AT);
	}
	
	@Override
	protected void initOutput() {
		pbt = new PerformBatUpdate(currentProfile);
	}
	
	@Override
	protected DbLayerSource initSource() {
		return new DbLayerSource();
	}
	
	@Override
	protected QuadifyImpl initQuadify() {
		return new QuadifyImpl(maxPerRect, TABLENAME, geomFieldName);
	}
	
	@Override
	protected List<Address> addressesForQuadrant(Envelope envelope) {
		
		String sql = "select * from " + TABLENAME
				+ " WHERE " + geomFieldName 
				+ " && " + DBUtils.stMakeEnvelope(envelope);
		IGenericDomain gd_ = LookupSessionBeans.genericDomain();

		List<Address> addresses = new ArrayList<>();
		
		AbstractDbTableReader tableReader = new AbstractDbTableReader() {

			@SuppressWarnings("unused")
			DbTable table;
			private DbGeometryField geomField;
			private DbTextField nameField;

			@Override
			public void metadata(DbTable table) {
				this.table = table;
				geomField = table.geometryField(geomFieldName);
				if (nameFieldName != null && !nameFieldName.trim().isEmpty())
				{
					nameField = table.textField(nameFieldName);
				}
			}

			@Override
			public void record(DbRecord record) {

				Geometry geom = geomField.get(record);
			
				//System.out.println(geom + " - " + name);

				Address currentAddress = new Address();
				currentAddress.geom = (Point)geom;
				
				if (nameField != null) {
					String name = nameField.get(record);
					currentAddress.name = name;
				}
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
