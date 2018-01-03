package at.qop.qoplib.dbbatch;

import java.util.ArrayList;
import java.util.List;

public class DbBatch {
	
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
				sb.append( record.values[i] + " {" +  record.sqlTypes[i] + "}");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

}
