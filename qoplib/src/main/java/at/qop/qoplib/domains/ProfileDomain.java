package at.qop.qoplib.domains;

import java.util.List;
import java.util.Set;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.vividsolutions.jts.geom.Geometry;

import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileAnalysis;
import at.qop.qoplib.entities.Analysis;

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
		org.hibernate.Query qry = hibSess().createQuery("from " + Profile.class.getSimpleName());
		return qry.list();
	}

	@Override
	public List<Analysis> listAnalyses() {
		org.hibernate.Query qry = hibSess().createQuery("from " + Analysis.class.getSimpleName());
		return qry.list();
	}
	
	@Override
	public void createProfile(Profile p) {
		hibSess().merge(p);
		System.out.println(p);
	}
	
	@Override
	public void updateProfile(Profile p) {
		hibSess().update(p);
		System.out.println(p);
	}

	@Override
	public void dropProfile(Profile p) {
		hibSess().update(p);
		hibSess().delete(p);
	}
	
	@Override
	public void createAnalysis(Analysis a) {
		hibSess().merge(a);
		System.out.println(a);
	}
	
	@Override
	public void updateAnalysis(Analysis a) {
		hibSess().update(a);
		System.out.println(a);
	}

	@Override
	public void dropAnalysis(Analysis a) {
		hibSess().update(a);
		hibSess().delete(a);
	}

	@Override
	public void createProfileAnalysis(Set<ProfileAnalysis> addedSelection) {
		addedSelection.forEach(pa -> {
			hibSess().merge(pa);
		}); 
		
	}

	@Override
	public void removeProfileAnalysis(Set<ProfileAnalysis> removedSelection) {
		removedSelection.forEach(pa -> {
			hibSess().createQuery("delete from " + ProfileAnalysis.class.getSimpleName() + " where id = " + pa.id).executeUpdate(); 
		}); 
	}
	
	@Override
	public void updateProfileAnalysis(ProfileAnalysis pa) {
		hibSess().update(pa);
	}

}
