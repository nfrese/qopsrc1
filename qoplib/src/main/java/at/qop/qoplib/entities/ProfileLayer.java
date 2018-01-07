package at.qop.qoplib.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="profilelayer")
public class ProfileLayer implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	public String tablename;
	
	@Id
	@ManyToOne
    @JoinColumn(name="profile_name", nullable=false)
	public Profile profile;

	//public int orderhint;
	@Column(length=1024)
	public String description;

	public String geomfield;
	public String query;
	
	@Column(columnDefinition="TEXT")
	public String evalfn;
	public double radius;
	
}
