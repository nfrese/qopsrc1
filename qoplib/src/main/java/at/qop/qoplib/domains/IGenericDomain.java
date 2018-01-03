package at.qop.qoplib.domains;

import java.sql.SQLException;

import at.qop.qoplib.dbbatch.DbBatch;
import at.qop.qoplib.dbbatch.AbstractDbTableReader;
import at.qop.qoplib.dbmetadata.QopDBMetadata;
import at.qop.qoplib.dbmetadata.QopDBTable;

public interface IGenericDomain {
	
	QopDBMetadata getMetadata();
	
	QopDBTable tableMetadata(String tname);

	void batchUpdate(DbBatch batch) throws SQLException;

	void readTable(String sql, AbstractDbTableReader tableReader) throws SQLException;

}
