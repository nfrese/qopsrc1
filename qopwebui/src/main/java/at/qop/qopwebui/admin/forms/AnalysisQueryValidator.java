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

package at.qop.qopwebui.admin.forms;

import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import com.vaadin.data.ValueContext;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.calculation.DbLayerSource.RastTableSQL;
import at.qop.qoplib.dbconnector.DBSingleResultTableReader;
import at.qop.qoplib.dbconnector.fieldtypes.DbGeometryField;
import at.qop.qoplib.domains.IGenericDomain;
import at.qop.qoplib.entities.Analysis;

public class AnalysisQueryValidator implements Validator<String> {

	private static final long serialVersionUID = 1L;
	private final AnalysisForm analysisForm;
	private Exception e;

	public AnalysisQueryValidator(AnalysisForm analysisForm) {
		this.analysisForm = analysisForm;
	}

	@Override
	public ValidationResult apply(String sql, ValueContext context) {
		if(check(sql)) {
			return ValidationResult.ok();
		} else {
			e.printStackTrace();
			return ValidationResult.error(e.getMessage());
		}
	}

	private boolean check(String sql) {
		IGenericDomain gd_ = LookupSessionBeans.genericDomain();
		DBSingleResultTableReader tableReader = new DBSingleResultTableReader();
		try {
			RastTableSQL rastTabelSQL = new RastTableSQL(sql);
			if (rastTabelSQL.isRasterTable())
			{
				gd_.readTable(rastTabelSQL.buildRasterSQL(new GeometryFactory().createPoint(new Coordinate(0,0))), tableReader);
			}
			else
			{
				gd_.readTable(sql + " LIMIT 1", tableReader);
			}
			if (analysisForm.analysis.geomfield != null && !analysisForm.analysis.geomfield.trim().isEmpty())
			{
				DbGeometryField geomField = tableReader.table.geometryField(analysisForm.analysis.geomfield);
				if ( geomField == null) throw new IllegalArgumentException("geomfield not in result");
			}
			else {
				throw new IllegalArgumentException("geomfield required");
			}
			return true;
		} catch (Exception e) {
			this.e = e;
			return false;
		}
	}

}
