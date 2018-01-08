package at.qop.qoplib.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="profile")
public class Profile implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	public String name;

	public String description;

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
