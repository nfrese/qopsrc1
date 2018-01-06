package at.qop.qoplib.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="profiles")
public class Profile implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	public String name;
	
	public String description;
	
	public String aggrfn;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "profile")
	public Set<ProfileLayer> profileLayer = new HashSet<>();
	

}
