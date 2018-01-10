package at.qop.qoplib.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="q_analysisfunction")
public class AnalysisFunction implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	public String name;
	
	@Column(length=2048)
	public String description;

	@Column(columnDefinition="TEXT")
	public String func;
	
	public String rvalUnit;
	
	@Override
	public String toString() {
		return name + " (" + description + ")";
	}
	
}
