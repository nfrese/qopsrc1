package at.qop.qoplib.domains;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.vividsolutions.jts.geom.Geometry;

import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileLayer;

@Stateless
@Local (IProfileDomain.class)
public class ProfileDomain extends AbstractDomain implements IProfileDomain {
	
	@PersistenceContext(unitName = "qopPU")
	EntityManager em_;

	public EntityManager em()
	{
		return em_;
	}

	@Override
	public List<Profile> listProfiles() {
		org.hibernate.Query qry = hibSess().createQuery("from Profile");
		return qry.list();
	}

	@Override
	public void createProfile(Profile profile) {
		hibSess().merge(profile);
		System.out.println(profile);
	}
	
	@Override
	public void updateProfile(Profile profile) {
		hibSess().update(profile);
		System.out.println(profile);
	}

	@Override
	public void createProfileLayer(ProfileLayer profile) {
		hibSess().update(profile);
		System.out.println(profile);
		//hibSess().persist(profile);
	}
	
	@Override
	public void dropProfile(Profile profile) {
		profile.profileLayer.clear();
		hibSess().update(profile);
		hibSess().delete(profile);
	}
	

}
