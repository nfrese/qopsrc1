package at.qop.qoplib.domains;

import java.sql.SQLException;
import java.util.List;

import at.qop.qoplib.dbbatch.DbBatch;
import at.qop.qoplib.dbbatch.DbRecord;
import at.qop.qoplib.dbmetadata.QopDBMetadata;
import at.qop.qoplib.entities.Config;

public interface IConfigDomain {
	
	List<Config> readConfiguration();

	QopDBMetadata getMetadata();

	void batchUpdate(DbBatch batch) throws SQLException;

}
