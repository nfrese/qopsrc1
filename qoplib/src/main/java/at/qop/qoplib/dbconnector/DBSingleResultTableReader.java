package at.qop.qoplib.dbconnector;

import java.util.ArrayList;
import java.util.List;

public final class DBSingleResultTableReader extends AbstractDbTableReader {
	
	public DbTable table = null;
	public List<DbRecord> records = new ArrayList<>();
	
	@Override
	public void metadata(DbTable table) {
		this.table = table;
	}

	@Override
	public void record(DbRecord record) {
		records.add(record);
	}
	
	public Object result() {
		return this.records.stream().findFirst().get().values[0];
	}
	
	public long longResult() {
		return (long)this.records.stream().findFirst().get().values[0];
	}
}