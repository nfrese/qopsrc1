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

import java.io.IOException;

import at.qop.qoplib.dbconnector.write.AbstractUpdater;

public class DropTable extends AbstractUpdater {
	
	final String tname;
	
	public DropTable(String tname) {
		super();
		this.tname = tname;
	}
	

	private String tname()
	{
		return tname;
	}
	
	@Override
	protected void before() {
		{
			String sql;
			sql = "DROP TABLE public." + tname();
			ddl(sql, false);
		}

		{
			String sql;
			sql = "DROP INDEX public." + tname() + "_geom_gist";
			ddl(sql, true);
		}
		done();
	}
	
	public void runUpdate() throws IOException {
		before();
	}


}
