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

package at.qop.qoplib.dbconnector;

import at.qop.qoplib.dbconnector.fieldtypes.DbDoubleField;
import at.qop.qoplib.dbconnector.fieldtypes.DbFieldAbstract;
import at.qop.qoplib.dbconnector.fieldtypes.DbFloat4Field;
import at.qop.qoplib.dbconnector.fieldtypes.DbFloat8Field;
import at.qop.qoplib.dbconnector.fieldtypes.DbGeometryField;
import at.qop.qoplib.dbconnector.fieldtypes.DbTextField;

public class DbTable {

	private static final int[] EMPTY_INT_ARR = new int[0];
	private static final String[] EMPTY_STRINGARR = new String[0];
	
	public String[] colNames = EMPTY_STRINGARR;
	public int[] sqlTypes = EMPTY_INT_ARR;
	public String[] typeNames = EMPTY_STRINGARR;
	
	public void init(int cols)
	{
		this.sqlTypes = new int[cols];
		this.colNames = new String[cols];	
		this.typeNames = new String[cols];
	}

	public DbGeometryField geometryField(String colName) {
		return this.field(colName, DbGeometryField.class);
	}

	public DbDoubleField doubleField(String colName) {
		return this.field(colName, DbDoubleField.class);
	}
	
	public DbFloat8Field float8Field(String colName) {
		return this.field(colName, DbFloat8Field.class);
	}

	public DbFloat4Field float4Field(String colName) {
		return this.field(colName, DbFloat4Field.class);
	}
	
	public DbTextField textField(String colName) {
		return this.field(colName, DbTextField.class);
	}
	
	public <T extends DbFieldAbstract> T field(String colName, Class<T> clazz) {
		T col;
		try {
			col = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		col.table = this;
		col.name = colName;
		for (int i=0;i<colNames.length;i++)
		{
			if (colName.equals(colNames[i])) {
				col.ix = i;
				col.checkFieldType(typeNames[i]);
				return col; 
			}
		}
		return null;
	}
	
}
