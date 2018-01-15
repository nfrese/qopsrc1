package at.qop.qoplib.batch;

import java.sql.SQLException;
import java.util.Collection;

import com.vividsolutions.jts.geom.Envelope;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.dbconnector.DBSingleResultTableReader;
import at.qop.qoplib.domains.IGenericDomain;

public class QuadifyImpl extends Quadify {

	final String tableName;
	
	public QuadifyImpl(int maxPerRect, String tableName) {
		super(maxPerRect);
		this.tableName = tableName;
	}
	
	@Override
	protected Envelope extent() {
		IGenericDomain gd_ = LookupSessionBeans.genericDomain();
		try {
			DBSingleResultTableReader tableReader = new DBSingleResultTableReader();
			gd_.readTable("SELECT ST_Extent(geom) as table_extent FROM " + tableName, tableReader);
			Object res = tableReader.result();
			return parseEnvelope(res);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private Envelope parseEnvelope(Object res) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int count(Envelope envelope) {
		IGenericDomain gd_ = LookupSessionBeans.genericDomain();
		try {
			DBSingleResultTableReader tableReader = new DBSingleResultTableReader();
			gd_.readTable("select count(*) from " + tableName, tableReader);
			return (int)tableReader.longResult();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Collection<?> list(Envelope envelope) {
		throw new RuntimeException("NIMPH");
	}

}
