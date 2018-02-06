package at.qop.qoplib.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import at.qop.qoplib.calculation.ILayerCalculationP1Params;

@Entity
@Table(name="q_analysis")
public class Analysis implements Serializable, ILayerCalculationP1Params {
	
	private static final long serialVersionUID = 1L;

	@Id
	public String name;
	
	@Column(length=2048)
	public String description;

	@Column(length=2048)
	public String query;
	public String geomfield;
	
	@ManyToOne
    @JoinColumn(name="evalfunction_name", nullable=true)
	public AnalysisFunction analysisfunction;
	
	@Column(columnDefinition="TEXT")
	public String ratingfunc;
	
	public double radius;
	
	@Enumerated(EnumType.STRING)
	public ModeEnum mode = ModeEnum.air;
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "analysis")
	public List<ProfileAnalysis> profileAnalysis = new ArrayList<>();
	
	public boolean hasRadius() {
		return radius != 0.0;
	}

	@Override
	@Transient
	public String getQuery() {
		return query;
	}

	@Override
	@Transient
	public double getRadius() {
		return radius;
	}

	@Override
	@Transient
	public String getGeomfield() {
		return geomfield;
	}

	public boolean travelTimeRequired() {
		return this.mode != null && this.mode != ModeEnum.air;
	}	
	
	public String batColumnName()
	{
		return this.name.replace("/", "_").replace("-", "_").replace(" ", "_");
	}

}
