package at.qop.qoplib.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.vividsolutions.jts.geom.Point;

@Entity
@Table(name="addresses")
public class Address implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	public int gid;
	
	public String name;
	
	public Point geom;
	
	public double zug_x;
	
	public double zug_y;
	
	@Override
	public String toString() {
		return name;
	}

}
