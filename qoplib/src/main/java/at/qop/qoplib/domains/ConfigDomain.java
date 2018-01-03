package at.qop.qoplib.domains;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Criteria;

import at.qop.qoplib.entities.Config;

@Stateless
@Local (IConfigDomain.class)
public class ConfigDomain extends AbstractDomain implements IConfigDomain {
	
	@PersistenceContext(unitName = "qopPU")
	EntityManager em_;

	public EntityManager em()
	{
		return em_;
	}

	
	
	@Override
	public List<Config> readConfiguration() {
		Criteria crit = hibSess().createCriteria(Config.class);
		return crit.list();
	}

}
