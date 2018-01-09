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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import at.qop.qoplib.calculation.ILayerCalculationP1Params;

@Entity
@Table(name="analysis")
public class Analysis implements Serializable, ILayerCalculationP1Params {
	
	private static final long serialVersionUID = 1L;

	@Id
	public String name;
	
	@Column(length=2048)
	public String description;

	@Column(length=2048)
	public String query;
	public String geomfield;
	
	@Column(columnDefinition="TEXT")
	public String evalfn;
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
	
}
