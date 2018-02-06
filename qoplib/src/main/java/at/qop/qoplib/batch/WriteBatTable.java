package at.qop.qoplib.batch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.dbconnector.DbBatch;
import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.write.AbstractUpdater;
import at.qop.qoplib.entities.Profile;

public class WriteBatTable extends AbstractUpdater {
	
	public static class BatRecord {
		public BatRecord(int size) {
			colGrps = new ColGrp[size];
		}
		String name;
		Point geom;
		ColGrp[] colGrps;
	}
	
	public static class ColGrp {
		String name;
		double result;
		double rating;
	}
	
	private Profile currentProfile;
	protected Map<String,Integer> columnsMap = new HashMap<>(); 

	public WriteBatTable(Profile currentProfile) {
		super();
		this.currentProfile = currentProfile;
	}
	
	private List<String> names()
	{
		return this.currentProfile.profileAnalysis.stream().map(a -> a.analysis.batColumnName()).collect(Collectors.toList());
	}
	
	private String tname()
	{
		return "bat_" + currentProfile.name;
	}
	
	@Override
	protected void before() {
		{
			String sql;
			sql = "DROP TABLE public." + tname();
			ddl(sql, true);
		}

		{
			String sql;
			sql = "DROP INDEX public." + tname() + "_geom_gist";
			ddl(sql, true);
		}
		
		{
			StringBuilder sb = new StringBuilder();
			int col = 0;

			sb.append("CREATE TABLE public." + tname()); 
			sb.append( " (" );
			sb.append( "  gid serial," ); 
			sb.append( "  name character varying(254)," ); columnsMap.put("name", col++);
			sb.append( "  geom geometry(Point)," ); columnsMap.put("geom", col++);

			for (String n : names()) { 
				sb.append(" " + n); columnsMap.put(n, col++);
				sb.append(" numeric,");
				sb.append(" r_" + n); columnsMap.put("r_" + n, col++);
				sb.append(" numeric,");
			};
			
			sb.append( "  CONSTRAINT " + tname() + "_pkey PRIMARY KEY (gid)" );
			sb.append( ")" );
			sb.append( "WITH (" );
			sb.append( "OIDS=FALSE" );
			sb.append( ");");
			ddl(sb.toString(), false);
		}
		{
			String sql = "ALTER TABLE public." + tname() 
					+ " OWNER TO qopuser";
			ddl(sql, false);
		}

		{
			String sql = "CREATE INDEX " + tname() + "_geom_gist"
					+ " ON public." + tname() 
					+ " USING gist"
					+ "(geom);";
			ddl(sql, false);
		}
		done();
	}


	public void insert(BatRecord record) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("insert into " + tname() + " (name, geom,");
		{
			int cnt = 0;
			for (String n : names()) {
				if (cnt > 0) sb.append(",");
				sb.append(" " + n + ""); 
				sb.append(", r_" + n + "");
				cnt++;
			};
		}
		
		sb.append(") values (?, ST_GeomFromText(?, 4326), ");
		{
			int cnt = 0;
			for (String n : names()) {
				if (cnt > 0) sb.append(",");
				sb.append(" ?"); 
				sb.append(", ?");
				cnt++;
			};
		}
		sb.append(")");
		
		DbBatch b = new DbBatch();
		b.sql = sb.toString();
		
		int ncols = 2 + record.colGrps.length * 2;
		{
			b.sqlTypes = new int[ncols];
			int col = 0;
			b.sqlTypes[col++] = java.sql.Types.VARCHAR;
			b.sqlTypes[col++] = java.sql.Types.VARCHAR;
			for (int i = 0; i < record.colGrps.length ; i++)
			{
				b.sqlTypes[col++] = java.sql.Types.DOUBLE;
				b.sqlTypes[col++] = java.sql.Types.DOUBLE;
			}
		}
		
		{
			DbRecord rec = new DbRecord();
			rec.values = new Object[ncols];
			int col = 0;
			rec.values[col++] = record.name;
			rec.values[col++] = record.geom;
			for (int i = 0; i < record.colGrps.length ; i++)
			{
				ColGrp g = record.colGrps[i];
				int ixResult = columnsMap.get(g.name);
				int ixRating = columnsMap.get("r_" + g.name);
				
				rec.values[ixResult] = g.result;
				rec.values[ixRating] = g.rating;
			}
			b.add(rec);
		}
		queue(b);
	}

	public void insert(BatRecord[] batRecs) {
		for (int i = 0; i < batRecs.length; i++)
		{
			insert(batRecs[i]);
		}
	}
}
