package at.qop.qoplib.domains;

import java.sql.SQLException;

import at.qop.qoplib.dbconnector.AbstractDbTableReader;
import at.qop.qoplib.dbconnector.DbBatch;
import at.qop.qoplib.dbconnector.metadata.QopDBMetadata;
import at.qop.qoplib.dbconnector.metadata.QopDBTable;

public interface IGenericDomain {
	
	QopDBMetadata getMetadata();
	
	QopDBTable tableMetadata(String tname);

	void batchUpdate(DbBatch batch) throws SQLException;

	void readTable(String sql, AbstractDbTableReader tableReader) throws SQLException;

}
