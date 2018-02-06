package at.qop.qoplib.calculation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileAnalysis;

public class Calculation {
	
	private final Profile profile;
	private final Point start;
	private final LayerSource source;
	private final IRouter router;
	
	public List<LayerCalculation> layerCalculations = new ArrayList<>();
	private Rating<ILayerCalculation> rating = null; 
	
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
			long t_start = System.currentTimeMillis();
			
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
			
			long t_finished = System.currentTimeMillis();
			System.out.println(">>>>" + profileAnalysis.analysis.name + " done in " + (t_finished - t_start) + "ms");
		};
		
		List<ILayerCalculation> x = new ArrayList<>(layerCalculations);
		this.rating = new Rating<ILayerCalculation>(profile,  new SectionBuilder<ILayerCalculation>(x).run());
		this.rating.runRating();
	}

	public double getOverallRating() {
		if (rating != null) return rating.overallRating;
		return Double.NaN;
	}

	public void runRating() {
		if (rating != null) rating.runRating();
	}

	public List<CalculationSection<ILayerCalculation>> getSections() {
		if (rating != null) return rating.sections;
		return Collections.emptyList();
	}
}
