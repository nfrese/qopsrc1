package at.qop.qoplib.dbconnector.write;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import at.qop.qoplib.dbconnector.DbBatch;
import at.qop.qoplib.dbconnector.DbRecord;
import au.com.bytecode.opencsv.CSVReader;

public abstract class AbstractUpdater {
	
	protected Map<String,Integer> columnsMap = new HashMap<>(); 
	
	DbBatch queue = null;

	private Function<DbBatch, Void> emitFn;
	
	public void onPacket(Function<DbBatch,Void> emitFn)
	{
		this.emitFn = emitFn;
	}
	
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

	protected void before()
	{
	}
	
	protected void emit(DbBatch b) {
		if (emitFn != null) emitFn.apply(b);
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
