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

import java.util.function.Function;

import at.qop.qoplib.dbconnector.DbBatch;
import at.qop.qoplib.dbconnector.DbRecord;

public abstract class AbstractUpdater {
	
	private DbBatch queue = null;

	private Function<DbBatch, Void> emitFn;
	
	public void onPacket(Function<DbBatch,Void> emitFn)
	{
		this.emitFn = emitFn;
	}


	protected void before()
	{
	}
	
	protected void queue(DbBatch b) {
		if (queue == null) {
			queue = b;
		} else {
			if (queue.canAppend(b))
			{
				queue.append(b);
			}
			else
			{
				emit(queue);
				queue = b;
			}
		}
		if (queue.size() > 1000)
		{
			emit(queue);
			queue = null;
		}
	}

	public void done()
	{
		if (queue != null)
		{
			emit(queue);
			queue = null;
		}
	}
	
	protected void emit(DbBatch b) {
		if (emitFn != null) emitFn.apply(b);
	}

	protected void ddl(String sql, boolean mayFail) {
		DbBatch b = new DbBatch();
		b.mayFail = mayFail;
		b.sql = sql;
		b.add(new DbRecord());
		queue(b);
	}


}
