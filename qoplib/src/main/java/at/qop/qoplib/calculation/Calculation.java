/* 
 * Copyright (C) 2018 Norbert Frese
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
*/

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
