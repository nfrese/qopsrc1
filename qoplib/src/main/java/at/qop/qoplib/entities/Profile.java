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

package at.qop.qoplib.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="q_profile")
public class Profile implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	public String name;

	public String description;

	@Column(columnDefinition="TEXT")
	public String aggrfn;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "profile")
	public List<ProfileAnalysis> profileAnalysis = new ArrayList<>();

	@Override
	public String toString() {
		return name;
	}

	public List<Analysis> listAnalysis() {
		List<Analysis> result = new ArrayList<>();
		profileAnalysis.forEach(pa -> { result.add(pa.analysis); });
		return result;
	}

}
