package at.qop.qoplib.dbbatch;

public abstract class DbTableReader {
	
	public boolean cancelled = false;
	
	public abstract void metadata(DbTable table);
	
	public abstract void record(DbRecord record);

	public void done() {}

}
