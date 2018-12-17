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

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTReader;

import at.qop.qoplib.batch.WriteBatTable.BatRecord;
import at.qop.qoplib.calculation.DbLayerSource;
import at.qop.qoplib.calculation.ILayerCalculationP1Params;
import at.qop.qoplib.calculation.LayerCalculationP1Result;
import at.qop.qoplib.calculation.LayerSource;
import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Profile;

public abstract class BatchCalculationInMemory extends BatchCalculationAbstract {

	private List<BatRecord> output;

	private QuadifyInMemory<Address> quad;
	
	public BatchCalculationInMemory(Profile currentProfile, List<Address> addresses) {
		super(currentProfile);
		quad = new QuadifyInMemory<Address>(maxPerRect);
		for (Address address: addresses)
		{
			quad.add(address.geom, address);
		}
	}

	@Override
	protected void initOutput() {
		output = new ArrayList<>();
	}

	
	@Override
	protected Quadify initQuadify() {
		return quad;
	}
	
	@Override
	protected List<Address> addressesForQuadrant(Envelope envelope) {
		
		ArrayList<Address> addresses = new ArrayList<Address>();
		addresses.addAll(quad.list(envelope));
		return addresses;
	}
	
	@Override
	protected void outputRecs(BatRecord[] batRecs) {
		for (BatRecord r : batRecs)
		{
			output.add(r);
		}
	}
	
	@Override
	protected void outputDone() {
	}

	@Override
	protected void failed(Throwable t) {
		throw new RuntimeException(t);
	}

	public List<BatRecord> getOutput() {
		return output;
	}
	
}
