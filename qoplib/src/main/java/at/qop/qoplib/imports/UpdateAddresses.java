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

package at.qop.qoplib.imports;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import at.qop.qoplib.dbconnector.DbBatch;
import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.write.AbstractCSVUpdater;
import at.qop.qoplib.entities.Address;

public class UpdateAddresses extends AbstractCSVUpdater {
	
	private final String bezirkfilter;
	public int recordCount = 0;
	
	public UpdateAddresses(String bezirkfilter) {
		super();
		this.bezirkfilter = bezirkfilter;
	}
	
	@Override
	protected void before() {
		{
			String sql;
			sql = "DROP TABLE public.q_addresses";
			ddl(sql, true);
		}
		
		{
			String sql = "CREATE TABLE public.q_addresses"
					+ " ("
					+ "  gid serial,"
					+ "  name character varying(254),"
					+ "  geom geometry(Point),"
					+ "  zug_x numeric,"
					+ "  zug_y numeric,"
					+ "  CONSTRAINT q_addresses_pkey PRIMARY KEY (gid)"
					+ ")"
					+ "WITH ("
					+ "OIDS=FALSE"
					+ ");";
			ddl(sql, false);
		}
		{
			String sql = "ALTER TABLE public.q_addresses"
					+ " OWNER TO qopuser";
			ddl(sql, false);
		}

		{
			String sql = "CREATE INDEX q_addresses_geom_gist"
					+ " ON public.q_addresses"
					+ " USING gist"
					+ "(geom);";
			ddl(sql, false);
		}

		{
			String sql = "CREATE INDEX q_adresses_name_ix"
					+ " ON public.q_addresses (name varchar_ops DESC NULLS FIRST);";
			ddl(sql, false);
		}
	}

	
	protected InputStream inputStream() throws IOException {
		String link = "https://data.wien.gv.at/daten/geo?service=WFS&request=GetFeature&version=1.1.0&typeName=ogdwien:ADRESSENOGD&srsName=EPSG:4326&outputFormat=csv";
		if (bezirkfilter != null)
		{
			link += "&cql_filter=GEB_BEZIRK='"+ bezirkfilter + "'";
		}
				
		URL url = new URL(link);
		return url.openConnection().getInputStream();
	}

	@Override
	public DbBatch gotRecord(String[] arr) {
		recordCount++;
		
		int nameIx = this.columnsMap.get("NAME");
		int shapeIx = this.columnsMap.get("SHAPE");
		int zugxIx = this.columnsMap.get("ZUG_X");
		int zugyIx = this.columnsMap.get("ZUG_Y");
		
		DbBatch b = new DbBatch();
		b.sql = "insert into " + Address.TABLENAME + " (name, geom, zug_x, zug_y) values (?, ST_GeomFromText(?, 4326), ?, ?)";
		b.sqlTypes = new int[] {
				java.sql.Types.VARCHAR, 
				java.sql.Types.VARCHAR, 
				java.sql.Types.DOUBLE, 
				java.sql.Types.DOUBLE};
		DbRecord rec = new DbRecord();
		rec.values = new String[] {
				arr[nameIx], 
				arr[shapeIx], 
				arr[zugxIx], 
				arr[zugyIx]};
		b.add(rec);
		
		return b;
	}


	

}
