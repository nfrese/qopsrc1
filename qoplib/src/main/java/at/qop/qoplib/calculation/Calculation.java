package at.qop.qoplib.calculation;

import java.util.ArrayList;
import java.util.List;

import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileAnalysis;

public class Calculation {
	
	private final Profile profile;
	private final Address address;
	private final LayerSource source;
	private final IRouter router;
	
	public List<LayerCalculation> layerCalculations = new ArrayList<>();
	
	public Calculation(Profile profile, Address address, LayerSource source, IRouter router) {
		super();
		this.profile = profile;
		this.address = address;
		this.source = source;
		this.router = router;
	}
	
	public void run()
	{
		for (ProfileAnalysis profileAnalysis : profile.profileAnalysis) {
			LayerCalculation lc = new LayerCalculation(address.geom, profileAnalysis.analysis, profileAnalysis.weight, profileAnalysis.altratingfunc);
			layerCalculations.add(lc);
			lc.p0loadTargets(source);
			lc.p1calcDistances();
			lc.p2travelTime(router);
			lc.p3OrderTargets();
			lc.p4Calculate();
			lc.p5route(router);
		};
	}
	
	public double overallRating()
	{
		return layerCalculations.stream().mapToDouble(lc -> (lc.rating * lc.weight)).sum();
	}

}
