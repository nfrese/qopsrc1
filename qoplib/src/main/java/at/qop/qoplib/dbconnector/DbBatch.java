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

import java.util.ArrayList;
import java.util.List;

public class DbBatch extends DbTable {
	
	public boolean mayFail = false;
	public String sql;
	private List<DbRecord> records = new ArrayList<>();
	
	public boolean canAppend(DbBatch b) {
		if (b.sql.equals(sql))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void append(DbBatch b)
	{
		if  (!canAppend(b)) throw new IllegalArgumentException();
		records().addAll(b.records());
	}

	public int size() {
		return records().size();
	}

	public void add(DbRecord rec) {
		records().add(rec);
	}

	public List<DbRecord> records() {
		return records;
	}
	
	public String toString()
	{
		StringBuilder sb  = new StringBuilder();
		sb.append(sql + "\n");
		for (DbRecord record : records)
		{
			for (int i = 0; i <record.values.length ; i++)
			{
				if (i > 0) sb.append(", ");
				sb.append( record.values[i] + " {" +  this.sqlTypes[i] + "}");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

}
