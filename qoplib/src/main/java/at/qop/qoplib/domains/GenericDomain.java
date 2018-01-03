package at.qop.qoplib.domains;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.EntityMode;

import at.qop.qoplib.dbbatch.DbBatch;
import at.qop.qoplib.dbbatch.DbRecord;
import at.qop.qoplib.dbbatch.DbTable;
import at.qop.qoplib.dbbatch.AbstractDbTableReader;
import at.qop.qoplib.dbmetadata.QopDBColumn;
import at.qop.qoplib.dbmetadata.QopDBMetadata;
import at.qop.qoplib.dbmetadata.QopDBTable;

@Stateless
@Local (IGenericDomain.class)
public class GenericDomain extends AbstractDomain implements IGenericDomain {
	
	@PersistenceContext(unitName = "qopPU")
	EntityManager em_;

	public EntityManager em()
	{
		return em_;
	}
	
	@Override
	public QopDBMetadata getMetadata()
	{
		QopDBMetadata metaOut = new QopDBMetadata(); 
		try {
			DatabaseMetaData metadata = hibSessImplementor().connection().getMetaData();
			
			
			ResultSet tableTypes = metadata.getTableTypes();
			while (tableTypes.next())
			{
				String TABLE_TYPE = tableTypes.getString(1);
				//System.out.println(TABLE_TYPE);
			}
			
			try (ResultSet tables = metadata.getTables(null, null, null, new String[] { "TABLE" } )) {

				Set<String> regularTableNames = new TreeSet<String>();

				while (tables.next())
				{
					String TABLE_NAME = tables.getString("TABLE_NAME");
					//System.out.println(TABLE_NAME);
					regularTableNames.add(TABLE_NAME);
				}

				for (String tname : regularTableNames)
				{
					QopDBTable t = tableMetadata(metadata, tname);
					metaOut.tables.add(t);
				}
			}
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return metaOut;
		
	}

	@Override
	public QopDBTable tableMetadata(String tname)
	{
		try {
			DatabaseMetaData metadata = hibSessImplementor().connection().getMetaData();
			return tableMetadata(metadata, tname);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private QopDBTable tableMetadata(DatabaseMetaData metadata, String tname) throws SQLException {
		QopDBTable t = new QopDBTable();
		t.name = tname;

		try (ResultSet columns = metadata.getColumns(null, null, tname, null)) {
			while (columns.next())
			{
				String COLUMN_NAME  = columns.getString("COLUMN_NAME");
				String TYPE_NAME  = columns.getString("TYPE_NAME");

				//System.out.println(tname + "; " + COLUMN_NAME + "; " + TYPE_NAME);

				QopDBColumn c = new QopDBColumn();
				c.name = COLUMN_NAME;
				c.typename = TYPE_NAME;

				t.columns.add(c);

			}
		}
		return t;
	}
	
	@Override
	public void batchUpdate(DbBatch batch) throws SQLException {
		
		Connection connection = hibSessImplementor().connection();
		
		try (PreparedStatement ps = connection.prepareStatement(batch.sql)) {

			for (DbRecord record: batch.records()) {

				for (int i = 0; i <record.values.length ; i++)
				{
					ps.setObject(i + 1, record.values[i], batch.sqlTypes[i]);
				}
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}
	
	@Override
	public void readTable(String sql, AbstractDbTableReader tableReader) throws SQLException
	{
		Connection connection = hibSessImplementor().connection();
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			try (ResultSet rs = ps.executeQuery()) {

				ResultSetMetaData metaData = rs.getMetaData();

				int cols = metaData.getColumnCount();
				
				DbTable table = new DbTable();
				table.init(cols);

				for (int i = 0; i< cols; i++)
				{
					table.sqlTypes[i] = metaData.getColumnType(i+1);
					table.colNames[i] = metaData.getColumnName(i+1);
				}

				tableReader.metadata(table);
				
				while (!tableReader.cancelled && rs.next()) {
					DbRecord rec = new DbRecord();

					rec.values = new Object[cols];
					for (int i = 0; i < cols ; i++)
					{
						Object value = rs.getObject(i+1);
						rec.values[i] = value;
					}
					tableReader.record(rec);
				}
			}
		}
		tableReader.done();
	}


}
