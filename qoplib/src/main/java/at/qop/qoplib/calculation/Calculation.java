package at.qop.qoplib.calculation;

import java.util.ArrayList;
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
	
	public List<CalculationSection> sections = new ArrayList<>();
	public double overallRating;
	
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
		
		new CalculationOrderer(this).run();
	}
	
	public void runRating()
	{
		OverallResult orc;
		if (profile.aggrfn != null && !profile.aggrfn.trim().isEmpty())
		{
			orc = new ScriptedOverallResult(profile.aggrfn, sections);
		}
		else
		{
			orc = new OverallResult(sections);
		}

		orc.run();
		overallRating = orc.overallRating;
	}

	public void addSection(CalculationSection current) {
		sections.add(current);
	}

}
