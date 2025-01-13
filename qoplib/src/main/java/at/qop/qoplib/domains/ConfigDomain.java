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

import at.qop.qoplib.entities.Config;

@Repository
@Transactional
public class ConfigDomain extends AbstractDomain implements IConfigDomain {
	
	@PersistenceContext //(unitName = "qopPU")
	EntityManager em_;

	public EntityManager em()
	{
		return em_;
	}

	
	
	@Override
	public List<Config> readConfiguration() {
		var cr = hibSess().getCriteriaBuilder().createQuery(Config.class);
		return hibSess().createQuery(cr).getResultList();
	}

	@Override
	public void injectEm(EntityManager em) {
		this.em_ = em;
	}

}
