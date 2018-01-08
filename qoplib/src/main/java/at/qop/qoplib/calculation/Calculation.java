package at.qop.qoplib.calculation;

import java.util.ArrayList;
import java.util.List;

import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileLayer;

public class Calculation {
	
	private final Profile profile;
	private final Address address;
	private final LayerSource source;
	
	public List<LayerCalculation> layerCalculations = new ArrayList<>();
	
	public Calculation(Profile profile, Address address, LayerSource source) {
		super();
		this.profile = profile;
		this.address = address;
		this.source = source;
	}
	
	
	public void run()
	{
		for (ProfileLayer profileLayer : profile.profileLayer) {
		
			LayerCalculation lc = new LayerCalculation(address.geom, profileLayer);
			layerCalculations.add(lc);
			lc.p1loadTargets(source);
			lc.p2OrderTargets();
			lc.p3Calculate();
		}
	}

}
