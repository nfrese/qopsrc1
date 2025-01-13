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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.locationtech.jts.geom.Geometry;

import at.qop.qoplib.entities.Address;

@Repository
@Transactional
public class AddressDomain extends AbstractDomain implements IAddressDomain {
	
	@PersistenceContext //(unitName = "qopPU")
	EntityManager em_;

	public EntityManager em()
	{
		return em_;
	}
	
	@Override
	public List<Address> findAddresses(Geometry filter) {
		org.hibernate.query.Query qry = hibSess().createQuery("from Address where geom intersects :filtergeom");
		qry.setParameter("filtergeom", filter);
		return qry.list();
	}

	@Override
	public List<Address> findAddresses(String searchTxt) {
		org.hibernate.query.Query qry = hibSess().createQuery("from Address where lower(name) like lower(:searchTxt)");
		qry.setParameter("searchTxt", searchTxt);
		return qry.list();
	}

	@Override
	public List<Address> findAddresses(int offset, int limit, String namePrefix) {
		System.out.println(offset + " " + limit + " " + namePrefix);
		org.hibernate.query.Query qry = hibSess().createQuery("from Address where lower(name) like lower(:searchTxt) order by name");
		qry.setParameter("searchTxt", namePrefix + "%");
		qry.setFetchSize(limit);
		return qry.list();
	}

	@Override
	public int countAddresses(String namePrefix) {
		org.hibernate.query.Query qry = hibSess().createQuery("from Address where lower(name) like lower(:searchTxt)");
		qry.setParameter("searchTxt", namePrefix + "%");
		return qry.list().size();
	}
	
	@Override
	public void injectEm(EntityManager em) {
		this.em_ = em;
	}

	

}
