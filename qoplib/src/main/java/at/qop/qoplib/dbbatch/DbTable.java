package at.qop.qoplib.dbbatch;

public class DbTable {

	private static final int[] EMPTY_INT_ARR = new int[0];
	private static final String[] EMPTY_STRINGARR = new String[0];
	
	public String[] colNames = EMPTY_STRINGARR;
	public int[] sqlTypes = EMPTY_INT_ARR;
	
	public void init(int cols)
	{
		this.sqlTypes = new int[cols];
		this.colNames = new String[cols];		
	}
	
}
