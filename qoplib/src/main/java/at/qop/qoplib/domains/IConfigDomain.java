package at.qop.qoplib.domains;

import java.sql.SQLException;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

import at.qop.qoplib.dbbatch.DbBatch;
import at.qop.qoplib.dbbatch.DbRecord;
import at.qop.qoplib.dbmetadata.QopDBMetadata;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Config;

public interface IConfigDomain {
	
	List<Config> readConfiguration();

	QopDBMetadata getMetadata();

	void batchUpdate(DbBatch batch) throws SQLException;

	List<Address> findAddresses(Geometry filter);

	List<Address> findAddresses(String searchTxt);

	List<Address> findAddresses(int offset, int limit, String namePrefix);

	int countAddresses(String namePrefix);

}
