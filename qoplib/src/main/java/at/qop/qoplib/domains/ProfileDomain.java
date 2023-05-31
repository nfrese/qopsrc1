/* 
 * Copyright (C) 2018 Norbert Frese
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
*/

package at.qop.qoplib.domains;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import at.qop.qoplib.entities.Analysis;
import at.qop.qoplib.entities.AnalysisFunction;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileAnalysis;

@Repository
@Transactional
public class ProfileDomain extends AbstractDomain implements IProfileDomain {
	
	@PersistenceContext //(unitName = "qopPU")
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

	@Override
	public List<AnalysisFunction> listAnalysisFunctions() {
		org.hibernate.Query qry = hibSess().createQuery("from " + AnalysisFunction.class.getSimpleName());
		return qry.list();

	}
	
	@Override
	public void createAnalysisFunction(AnalysisFunction f) {
		hibSess().merge(f);
		System.out.println(f);
	}
	
	@Override
	public void updateAnalysisFunction(AnalysisFunction f) {
		hibSess().update(f);
		System.out.println(f);
	}

	@Override
	public void dropAnalysisFunction(AnalysisFunction f) {
		hibSess().update(f);
		hibSess().delete(f);
	}
	
	@Override
	public void injectEm(EntityManager em) {
		this.em_ = em;
	}


}
