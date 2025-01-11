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
import java.util.Collection;

import org.locationtech.jts.geom.Envelope;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.dbconnector.DBSingleResultTableReader;
import at.qop.qoplib.dbconnector.DBUtils;
import at.qop.qoplib.domains.IGenericDomain;

public class QuadifyImpl extends Quadify {

	private final String tableName;
	private final String geomField;
	
	public QuadifyImpl(int maxPerRect, String tableName, String geomField) {
		super(maxPerRect);
		this.tableName = tableName;
		this.geomField = geomField;
	}
	
	@Override
	protected Envelope extent() {
		IGenericDomain gd_ = LookupSessionBeans.genericDomain();
		try {
			DBSingleResultTableReader tableReader = new DBSingleResultTableReader();
			gd_.readTable("SELECT ST_Extent(" + geomField + ") as table_extent FROM " + tableName, tableReader);
			Object res = tableReader.result();
			return parseEnvelope(res);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private Envelope parseEnvelope(Object res) {
		return DBUtils.parsePGEnvelope(res);
	}

	@Override
	protected int count(Envelope envelope) {
		IGenericDomain gd_ = LookupSessionBeans.genericDomain();
		try {
			DBSingleResultTableReader tableReader = new DBSingleResultTableReader();
			String sql = "select count(*) from " + tableName 
					+ " WHERE " + geomField 
					+ " && " + DBUtils.stMakeEnvelope(envelope);
			gd_.readTable(sql, tableReader);
			int count =  (int)tableReader.longResult();
			//System.out.println("count=" + count);
			return count;

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Collection<?> list(Envelope envelope) {
		throw new RuntimeException("NIMPH");
	}

}
