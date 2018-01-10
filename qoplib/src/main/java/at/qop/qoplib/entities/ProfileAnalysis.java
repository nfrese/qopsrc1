package at.qop.qoplib.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="q_profileanalysis")
public class ProfileAnalysis implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
	public int id;
	
	@ManyToOne
    @JoinColumn(name="profile_name", nullable=false)
	public Profile profile;
	
	@ManyToOne
    @JoinColumn(name="analysis_name", nullable=false)
	public Analysis analysis;

	public double weight = 1;
	
	@Column(columnDefinition="TEXT")
	public String altratingfunc;
	
	public String category;
	
}
