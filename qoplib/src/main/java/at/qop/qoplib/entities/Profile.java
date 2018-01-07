package at.qop.qoplib.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

@Entity
@Table(name="profiles")
public class Profile implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	public String name;
	
	public String description;
	
	public String aggrfn;
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "profile", cascade = CascadeType.ALL)
	@OrderColumn(name = "orderhint")
	public List<ProfileLayer> profileLayer = new ArrayList<>();
	
	@Override
	public String toString() {
		return name;
	}
	
}
