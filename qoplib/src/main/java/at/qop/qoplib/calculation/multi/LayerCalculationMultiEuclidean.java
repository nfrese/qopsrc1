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

package at.qop.qoplib.calculation.multi;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.index.strtree.STRtree;

import at.qop.qoplib.calculation.CRSTransform;
import at.qop.qoplib.calculation.LayerCalculation;
import at.qop.qoplib.calculation.LayerTarget;
import at.qop.qoplib.calculation.MultiTarget;
import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.entities.ProfileAnalysis;

public class LayerCalculationMultiEuclidean extends LayerCalculation {
	
	private STRtree spatIx;
	
	public LayerCalculationMultiEuclidean(Point start, ProfileAnalysis params, double presetWeight, String altRatingFunc,
			DbTable table, STRtree spatIx) {
		super(start, params, presetWeight, altRatingFunc);
		this.table = table;
		this.spatIx = spatIx;
	}

	@Override
	public void p0loadTargets() {
		orderedTargets = new ArrayList<>();
		if (!analysis().hasRadius()) throw new RuntimeException("dont!");
		Geometry buffer = CRSTransform.singleton.bufferWGS84Corr(start, analysis().getRadius());
		
		@SuppressWarnings("unchecked")
		List<MultiTarget> results = spatIx.query(buffer.getEnvelopeInternal());
		
		for (MultiTarget mt : results)
		{
			LayerTarget lt = new LayerTarget();
			
			lt.geom = mt.geom;
			lt.rec = mt.rec;
			orderedTargets.add(lt);
		}
	}
	
	public void p2travelTime() {
	}

}
