package at.qop.qoplib.calculation;

import java.sql.SQLException;

import com.vividsolutions.jts.geom.Geometry;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.dbconnector.DbTableReader;
import at.qop.qoplib.domains.IGenericDomain;

public class DbLayerSource implements LayerSource {

	@Override
	public LayerCalculationP1Result load(Geometry start, ILayerCalculationP1Params layerParams) {

		IGenericDomain gd_ = LookupSessionBeans.genericDomain();
		try {
			DbTableReader tableReader = new DbTableReader();

			String sql = layerParams.getQuery();
			if (layerParams.hasRadius())
			{
				Geometry buffer = CRSTransform.singleton.bufferWGS84Corr(start, layerParams.getRadius());
				String stIntersectsSql = "ST_Intersects(" + layerParams.getGeomfield() + ", 'SRID=4326;" + buffer + "'::geometry)";
				if (sql.toUpperCase().contains("WHERE"))
				{
					sql += " AND " + stIntersectsSql;
				}
				else
				{
					sql += " WHERE "+ stIntersectsSql;
				}
			}
			gd_.readTable(sql, tableReader);
			LayerCalculationP1Result r = new LayerCalculationP1Result();
			r.table = tableReader.table;
			r.records = tableReader.records;
			return r;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}


	}

}
