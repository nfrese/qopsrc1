package at.qop.qoplib;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import at.qop.qoplib.dbbatch.DbBatch;
import at.qop.qoplib.dbbatch.DbRecord;

public class UpdateAddresses extends AbstractUpdater {
	
	@Override
	protected void before() {
		{
			String sql;
			sql = "DROP TABLE public.addresses";
			ddl(sql, true);
		}

		{
			String sql;
			sql = "DROP INDEX public.addresses_geom_gist";
			ddl(sql, true);
		}
		
		{
			String sql = "CREATE TABLE public.addresses"
					+ " ("
					+ "  gid serial,"
					+ "  name character varying(254),"
					+ "  geom geometry(Point),"
					+ "  zug_x numeric,"
					+ "  zug_y numeric,"
					+ "  CONSTRAINT addresses_pkey PRIMARY KEY (gid)"
					+ ")"
					+ "WITH ("
					+ "OIDS=FALSE"
					+ ");";
			ddl(sql, false);
		}
		{
			String sql = "ALTER TABLE public.addresses"
					+ " OWNER TO qopuser";
			ddl(sql, false);
		}

		{
			String sql = "CREATE INDEX addresses_geom_gist"
					+ " ON public.addresses"
					+ " USING gist"
					+ "(geom);";
			ddl(sql, false);
		}
	}

	
	protected InputStream inputStream() throws IOException {
		String link = "https://data.wien.gv.at/daten/geo?service=WFS&request=GetFeature&version=1.1.0&typeName=ogdwien:ADRESSENOGD&srsName=EPSG:4326&outputFormat=csv&cql_filter=GEB_BEZIRK=%2701%27";
		//link = "https://data.wien.gv.at/daten/geo?service=WFS&request=GetFeature&version=1.1.0&typeName=ogdwien:ADRESSENOGD&srsName=EPSG:4326&outputFormat=csv";
		URL url = new URL(link);
		return url.openConnection().getInputStream();
	}

	@Override
	public DbBatch gotRecord(String[] arr) {
		
		int nameIx = this.columnsMap.get("NAME");
		int shapeIx = this.columnsMap.get("SHAPE");
		int zugxIx = this.columnsMap.get("ZUG_X");
		int zugyIx = this.columnsMap.get("ZUG_Y");
		
		//String zuggeom;
		
		DbBatch b = new DbBatch();
		b.sql = "insert into addresses (name, geom, zug_x, zug_y) values (?, ST_GeomFromText(?, 4326), ?, ?)";
		DbRecord rec = new DbRecord();
		rec.sqlTypes = new int[] {
				java.sql.Types.VARCHAR, 
				java.sql.Types.VARCHAR, 
				java.sql.Types.DOUBLE, 
				java.sql.Types.DOUBLE};
		rec.values = new String[] {
				arr[nameIx], 
				arr[shapeIx], 
				arr[zugxIx], 
				arr[zugyIx]};
		b.add(rec);
		
		return b;
	}


	

}
