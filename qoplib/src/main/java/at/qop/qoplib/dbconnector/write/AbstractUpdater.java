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
