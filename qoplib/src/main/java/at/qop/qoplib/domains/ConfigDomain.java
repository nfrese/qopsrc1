package at.qop.qoplib.domains;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.Session;

import com.vividsolutions.jts.geom.Geometry;

import at.qop.qoplib.dbbatch.DbBatch;
import at.qop.qoplib.dbbatch.DbRecord;
import at.qop.qoplib.dbmetadata.QopDBColumn;
import at.qop.qoplib.dbmetadata.QopDBMetadata;
import at.qop.qoplib.dbmetadata.QopDBTable;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Config;

@Stateless
@Local (IConfigDomain.class)
public class ConfigDomain implements IConfigDomain {
	
	@PersistenceContext(unitName = "qopPU")
	EntityManager em;

	private Session hibSess()
	{
		return  (Session) em.getDelegate();
	}
	
	private org.hibernate.engine.spi.SessionImplementor hibSessImplementor()
	{
		return  (org.hibernate.engine.spi.SessionImplementor) em.getDelegate();
	}
	
	
	@Override
	public List<Config> readConfiguration() {
		Criteria crit = hibSess().createCriteria(Config.class);
		getMetadata();
		return crit.list();
	}
	
	@Override
	public List<Address> findAddresses(Geometry filter) {
		org.hibernate.Query qry = hibSess().createQuery("from Address where geom intersects :filtergeom");
		qry.setParameter("filtergeom", filter);
		return qry.list();
	}

	@Override
	public List<Address> findAddresses(String searchTxt) {
		org.hibernate.Query qry = hibSess().createQuery("from Address where name like :searchTxt");
		qry.setParameter("searchTxt", searchTxt);
		return qry.list();
	}
	

	@Override
	public List<Address> findAddresses(int offset, int limit, String namePrefix) {
		System.out.println(offset + " " + limit + " " + namePrefix);
		org.hibernate.Query qry = hibSess().createQuery("from Address where name like :searchTxt order by name");
		qry.setParameter("searchTxt", namePrefix + "%");
		qry.setFetchSize(limit);
		return qry.list();
	}

	@Override
	public int countAddresses(String namePrefix) {
		org.hibernate.Query qry = hibSess().createQuery("from Address where name like :searchTxt");
		qry.setParameter("searchTxt", namePrefix + "%");
		return qry.list().size();
	}
	
	@Override
	public QopDBMetadata getMetadata()
	{
		QopDBMetadata metaOut = new QopDBMetadata(); 
		try {
			DatabaseMetaData metadata = hibSessImplementor().connection().getMetaData();
			
			
			System.out.println(metadata.getDatabaseProductName());
			
			
			ResultSet tableTypes = metadata.getTableTypes();
			while (tableTypes.next())
			{
				String TABLE_TYPE = tableTypes.getString(1);
				System.out.println(TABLE_TYPE);
			}
			
			ResultSet tables = metadata.getTables(null, null, null, new String[] { "TABLE" } );
			
			Set<String> regularTableNames = new TreeSet<String>();
			
			while (tables.next())
			{
				String TABLE_NAME = tables.getString("TABLE_NAME");
				System.out.println(TABLE_NAME);
				regularTableNames.add(TABLE_NAME);
			}
				
			for (String tname : regularTableNames)
			{
				QopDBTable t = new QopDBTable();
				t.name = tname;
				metaOut.tables.add(t);
				
				ResultSet columns = metadata.getColumns(null, null, tname, null);
				while (columns.next())
				{
					String COLUMN_NAME  = columns.getString("COLUMN_NAME");
					String TYPE_NAME  = columns.getString("TYPE_NAME");
					
					System.out.println(tname + "; " + COLUMN_NAME + "; " + TYPE_NAME);
					
					QopDBColumn c = new QopDBColumn();
					c.name = COLUMN_NAME;
					c.typename = TYPE_NAME;
					
					t.columns.add(c);
					
				}
			}
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return metaOut;
		
	}
	
	@Override
	public void batchUpdate(DbBatch batch) throws SQLException {
		
		Connection connection = hibSessImplementor().connection();
		
		//String sql = "insert into employee (name, city, phone) values (?, ?, ?)";
		PreparedStatement ps = connection.prepareStatement(batch.sql);

		for (DbRecord record: batch.records()) {

			for (int i = 0; i <record.values.length ; i++)
			{
				ps.setObject(i + 1, record.values[i], record.sqlTypes[i]);
			}
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();
		//connection.close();				
	}


}
