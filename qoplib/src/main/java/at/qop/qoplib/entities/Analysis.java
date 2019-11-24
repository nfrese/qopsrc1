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

package at.qop.qoplib.entities;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.Utils;
import at.qop.qoplib.calculation.ILayerCalculationP1Params;
import at.qop.qoplib.calculation.DbLayerSource.RastTableSQL;
import at.qop.qoplib.dbconnector.DBSingleResultTableReader;
import at.qop.qoplib.dbconnector.fieldtypes.DbGeometryField;
import at.qop.qoplib.domains.IGenericDomain;

@Entity
@Table(name="q_analysis")
public class Analysis implements Serializable, ILayerCalculationP1Params {
	
	private static final long serialVersionUID = 1L;

	@Id
	public String name;
	
	@Column(length=2048)
	public String description;

	@Column(length=2048)
	public String query;
	public String geomfield;
	
	@ManyToOne
    @JoinColumn(name="evalfunction_name", nullable=true)
	public AnalysisFunction analysisfunction;
	
	@Column(columnDefinition="TEXT")
	public String ratingfunc;
	
	public double radius;
	
	@Enumerated(EnumType.STRING)
	public ModeEnum mode = ModeEnum.air;
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "analysis")
	public List<ProfileAnalysis> profileAnalysis = new ArrayList<>();
	
	public boolean hasRadius() {
		return radius != 0.0;
	}

	@Override
	@Transient
	public String getQuery() {
		return query;
	}

	@Override
	@Transient
	public double getRadius() {
		return radius;
	}

	@Override
	@Transient
	public String getGeomfield() {
		return geomfield;
	}

	public boolean travelTimeRequired() {
		return this.mode != null && this.mode != ModeEnum.air;
	}	
	
	public String batColumnName()
	{
		return Utils.toPGColumnName(this.name);
	}

	@Transient
	public String checkValid() {
		try {
			checkQuery(this, query);
		} catch (Exception ex) {
			return "FAIL";
		}
		return "OK";
	}
	
	public static void checkQuery(Analysis analysis, String query) throws SQLException {
		IGenericDomain gd_ = LookupSessionBeans.genericDomain();
		DBSingleResultTableReader tableReader = new DBSingleResultTableReader();
		RastTableSQL rastTabelSQL = new RastTableSQL(query);
		if (rastTabelSQL.isRasterTable())
		{
			gd_.readTable(rastTabelSQL.buildRasterSQL(new GeometryFactory().createPoint(new Coordinate(0,0))), tableReader);
		}
		else
		{
			gd_.readTable(query + " LIMIT 1", tableReader);
		}
		if (analysis.geomfield != null && !analysis.geomfield.trim().isEmpty())
		{
			DbGeometryField geomField = tableReader.table.geometryField(analysis.geomfield);
			if ( geomField == null) throw new IllegalArgumentException("geomfield not in result");
		}
		else {
			throw new IllegalArgumentException("geomfield required");
		}
	}

}
