package at.qop.qoplib.domains;

import javax.persistence.EntityManager;

import org.hibernate.Session;


public abstract class AbstractDomain {
	
	public abstract EntityManager em();
	
	protected Session hibSess()
	{
		return  (Session) em().getDelegate();
	}
	
	protected org.hibernate.engine.spi.SessionImplementor hibSessImplementor()
	{
		return  (org.hibernate.engine.spi.SessionImplementor) em().getDelegate();
	}


}
