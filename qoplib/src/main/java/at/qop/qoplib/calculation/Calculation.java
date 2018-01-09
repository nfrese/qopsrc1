package at.qop.qoplib.calculation;

import java.util.ArrayList;
import java.util.List;

import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.Analysis;

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
		for (Analysis analysis : profile.listAnalysis()) {
		
			LayerCalculation lc = new LayerCalculation(address.geom, analysis);
			layerCalculations.add(lc);
			lc.p0loadTargets(source);
			lc.p0calcDistances();
			lc.p1routeTargets(router);
			lc.p2OrderTargets();
			lc.p3Calculate();
		}
	}

}
