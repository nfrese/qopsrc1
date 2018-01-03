package at.qop.qoplib.domains;

import java.sql.SQLException;

import at.qop.qoplib.dbbatch.DbBatch;
import at.qop.qoplib.dbmetadata.QopDBMetadata;

public interface IGenericDomain {
	
	QopDBMetadata getMetadata();

	void batchUpdate(DbBatch batch) throws SQLException;

}
