package at.qop.qoplib.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="profiles")
public class Profile {
	
	String name;
	
	public Set<LayerParams> layerParams = new HashSet<>();

}
