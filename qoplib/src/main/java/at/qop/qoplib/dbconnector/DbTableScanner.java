package at.qop.qoplib.dbconnector;

public final class DbTableScanner extends AbstractDbTableReader {
	
	public DbTable table = null;
	
	@Override
	public void metadata(DbTable table) {
		this.table = table;
	}

	@Override
	public void record(DbRecord record) {
		cancelled = true;
	}
}