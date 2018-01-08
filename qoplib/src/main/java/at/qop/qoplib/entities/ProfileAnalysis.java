package at.qop.qoplib.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="profileanalysis")
public class ProfileAnalysis {
	
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
	
}
