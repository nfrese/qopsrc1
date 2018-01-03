package at.qop.qoplib.domains;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.vividsolutions.jts.geom.Geometry;

import at.qop.qoplib.entities.Address;

@Stateless
@Local (IAddressDomain.class)
public class AddressDomain extends AbstractDomain implements IAddressDomain {
	
	@PersistenceContext(unitName = "qopPU")
	EntityManager em_;

	public EntityManager em()
	{
		return em_;
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
	

}
