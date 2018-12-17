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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.calculation.CalculationSection;
import at.qop.qoplib.calculation.ILayerCalculation;
import at.qop.qoplib.calculation.ISectionBuilderInput;
import at.qop.qoplib.calculation.Rating;
import at.qop.qoplib.dbconnector.DbBatch;
import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.write.AbstractUpdater;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileAnalysis;

public class WriteBatTable extends AbstractUpdater {
	
	public static class BatRecord {
		public BatRecord(int size) {
			colGrps = new ColGrp[size];
		}
		public int gid;
		public String name;
		public Point geom;
		public ColGrp[] colGrps;
	}
	
	public static class ColGrp implements ILayerCalculation {
		public String name;
		public double result;
		protected double rating;
		
		protected ProfileAnalysis pa;

		@Override
		public ProfileAnalysis getParams() {
			return pa;
		}

		@Override
		public double getRating() {
			return rating;
		}

		@Override
		public double getWeight() {
			return pa.weight;
		}
	}
	
	private Profile currentProfile;
	protected Map<String,Integer> columnsMap = new HashMap<>();
	private final WriteSectionsHelper sectionsHelper; 
	

	public WriteBatTable(Profile currentProfile) {
		super();
		this.currentProfile = currentProfile;
		
		sectionsHelper = new WriteSectionsHelper(currentProfile);
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
			sb.append( "  overallrating numeric," ); columnsMap.put("overallrating", col++);

			for (String n : names()) { 
				sb.append(" " + n); columnsMap.put(n, col++);
				sb.append(" numeric,");
				sb.append(" r_" + n); columnsMap.put("r_" + n, col++);
				sb.append(" numeric,");
			};

			for (CalculationSection<ISectionBuilderInput> section : sectionsHelper.getSections())
			{
				String n = section.getSectionColumnName();
				sb.append(" " + n); columnsMap.put(n, col++);
				sb.append(" numeric,");
			}			

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
		sb.append("insert into " + tname() + " (name, geom, overallrating ");
		{
			for (String n : names()) {
				sb.append(", " + n + ""); 
				sb.append(", r_" + n + "");
			};
		}

		for (CalculationSection<ISectionBuilderInput> section : sectionsHelper.getSections())
		{
			sb.append(", ");
			sb.append(section.getSectionColumnName());
		}			

		sb.append(") values (?, ST_GeomFromText(?, 4326), ? ");
		
		{
			for (String n : names()) {
				sb.append(", ?"); 
				sb.append(", ?");
			};
		}
		
		for (int i = 0; i < sectionsHelper.numSections(); i++)
		{
			sb.append(", ?");
		}
		
		sb.append(")");
		
		DbBatch b = new DbBatch();
		b.sql = sb.toString();
		
		int ncols = 3 + record.colGrps.length * 2 + sectionsHelper.numSections();
		{
			b.sqlTypes = new int[ncols];
			int col = 0;
			b.sqlTypes[col++] = java.sql.Types.VARCHAR;
			b.sqlTypes[col++] = java.sql.Types.VARCHAR;
			b.sqlTypes[col++] = java.sql.Types.DOUBLE;
			for (int i = 0; i < record.colGrps.length ; i++)
			{
				b.sqlTypes[col++] = java.sql.Types.DOUBLE;
				b.sqlTypes[col++] = java.sql.Types.DOUBLE;
			}
			for (int i = 0; i < sectionsHelper.numSections(); i++)
			{
				b.sqlTypes[col++] = java.sql.Types.DOUBLE;
			}
		}
		
		{
			DbRecord rec = new DbRecord();
			
			Rating<ColGrp> rating = sectionsHelper.rating(record);
			
			rec.values = new Object[ncols];
			int col = 0;
			rec.values[col++] = record.name;
			rec.values[col++] = record.geom;
			rec.values[col++] = rating.overallRating;
			
			for (int i = 0; i < record.colGrps.length ; i++)
			{
				ColGrp g = record.colGrps[i];
				int ixResult = columnsMap.get(g.name);
				int ixRating = columnsMap.get("r_" + g.name);
				
				rec.values[ixResult] = g.result;
				rec.values[ixRating] = g.rating;
			}
			
			col += record.colGrps.length * 2;
			
			for (CalculationSection<ColGrp> section : rating.sections)
			{
				rec.values[col++] = section.rating;
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
