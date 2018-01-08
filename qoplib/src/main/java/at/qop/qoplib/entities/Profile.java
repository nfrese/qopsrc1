package at.qop.qoplib.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

@Entity
@Table(name="profiles")
public class Profile implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	public String name;

	public String description;

	public String aggrfn;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="profilelayer",
			joinColumns=@JoinColumn(name="profile_name")
	)
	public List<ProfileLayer> profileLayer = new ArrayList<>();

	@Override
	public String toString() {
		return name;
	}

}
