package at.qop.qoplib.calculation;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileAnalysis;

public class Calculation {
	
	private final Profile profile;
	private final Point start;
	private final LayerSource source;
	private final IRouter router;
	
	public List<LayerCalculation> layerCalculations = new ArrayList<>();
	
	public List<CalculationSection> sections = new ArrayList<>();
	
	public Calculation(Profile profile, Point address, LayerSource source, IRouter router) {
		super();
		this.profile = profile;
		this.start = address;
		this.source = source;
		this.router = router;
	}
	
	public void run()
	{
		for (ProfileAnalysis profileAnalysis : profile.profileAnalysis) {
			LayerCalculation lc = new LayerCalculationSingle(start, profileAnalysis, 
					profileAnalysis.weight, profileAnalysis.altratingfunc,
					source, router);
			layerCalculations.add(lc);
			lc.p0loadTargets();
			lc.p1calcDistances();
			lc.p2travelTime();
			lc.p3OrderTargets();
			lc.p4Calculate();
			lc.p5route(router);
		};
		
		new CalculationOrderer(this).run();
	}
	
	public double overallRating()
	{
		double sectionSum = sections.stream().mapToDouble(se -> se.rating()).sum();
		
		double overall = layerCalculations.stream().mapToDouble(lc -> (lc.rating * lc.weight)).sum();
		if ((int)(sectionSum*100) != (int)(overall*100)) throw new RuntimeException("never: " + sectionSum + " != " + overall);
		return overall;
	}

	public void addSection(CalculationSection current) {
		sections.add(current);
	}

}
