package at.qop.qoplib.dbmetadata;

import java.util.ArrayList;
import java.util.List;

public class QopDBTable {
	
	public String name;
	
	public List<QopDBColumn> columns = new ArrayList<>();
	
	public boolean isGeometric()
	{
		return columns.stream().filter(c -> "geometry".equals(c.typename )).count() > 0;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
