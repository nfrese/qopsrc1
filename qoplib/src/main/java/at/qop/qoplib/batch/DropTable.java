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
