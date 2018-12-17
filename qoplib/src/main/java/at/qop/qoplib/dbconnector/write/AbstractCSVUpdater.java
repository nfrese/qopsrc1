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

package at.qop.qoplib.dbconnector.write;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import at.qop.qoplib.dbconnector.DbBatch;
import at.qop.qoplib.dbconnector.DbRecord;
import au.com.bytecode.opencsv.CSVReader;

public abstract class AbstractCSVUpdater extends AbstractUpdater {
	
	protected Map<String,Integer> columnsMap = new HashMap<>(); 
	
	public void runUpdate() throws IOException {
		InputStream is = inputStream();
		
		before();

		try (CSVReader reader = new CSVReader(new InputStreamReader(is), ',')) { 

			int cnt = 0;

			String[] columnNames = null;

			columnsMap = new HashMap<>(); 

			while (true) {
				String[] arr = reader.readNext();
				if (arr == null) break;
				if (cnt == 0)
				{
					columnNames = arr;
					int col = 0;
					for (String name : columnNames)
					{
						columnsMap.put(name, col);
						col++;
					}
				}
				else
				{
					queue(gotRecord(arr));
				}
				cnt++;
			}
		}

	}

	protected abstract  InputStream inputStream() throws IOException;

	public abstract DbBatch gotRecord(String[] arr);

	protected void ddl(String sql, boolean mayFail) {
		DbBatch b = new DbBatch();
		b.mayFail = mayFail;
		b.sql = sql;
		b.add(new DbRecord());
		queue(b);
	}


}
