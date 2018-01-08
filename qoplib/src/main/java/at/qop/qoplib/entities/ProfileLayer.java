package at.qop.qoplib.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ProfileLayer implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public String tablename;
	
	@Column(length=1024)
	public String description;

	public String geomfield;
	public String query;
	
	@Column(columnDefinition="TEXT")
	public String evalfn;
	public double radius;
	
	public boolean hasRadius() {
		return radius != 0.0;
	}
	
}
