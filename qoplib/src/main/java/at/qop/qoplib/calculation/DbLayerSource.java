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
