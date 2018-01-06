package at.qop.qoplib.entities;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="layerparams")
public class LayerParams {
	
	public int order;
	public String table;
	public String geomfield;
	public String fields;
	public String fn;
	public String description;
	public double radius;
}
