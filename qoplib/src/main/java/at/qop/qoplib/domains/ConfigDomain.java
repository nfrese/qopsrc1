package at.qop.qoplib.domains;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import at.qop.qoplib.entities.Config;

@Stateless
@Local (IConfigDomain.class)
public class ConfigDomain implements IConfigDomain {
	
	@PersistenceContext(unitName = "qopPU")
	EntityManager em;

	public List<Config> readConfiguration() {
		Query qry = em.createQuery("from Config");
		return qry.getResultList();
	}
	

}
