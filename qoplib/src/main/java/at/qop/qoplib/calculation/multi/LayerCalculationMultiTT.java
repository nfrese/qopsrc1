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

import org.locationtech.jts.geom.Point;

import at.qop.qoplib.calculation.LayerCalculation;
import at.qop.qoplib.calculation.LayerTarget;
import at.qop.qoplib.calculation.MultiTarget;
import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.entities.ProfileAnalysis;

public class LayerCalculationMultiTT extends LayerCalculation {
	
	private double[][] times;
	private int timesRow;
	private ArrayList<MultiTarget> multiTargets;
	
	public LayerCalculationMultiTT(Point start, ProfileAnalysis params, double presetWeight, String altRatingFunc,
			DbTable table,
			ArrayList<MultiTarget> multiTargets,
			double[][] times,
			int timesRow) {
		super(start, params, presetWeight, altRatingFunc);
		this.table = table;
		this.multiTargets = multiTargets;
		this.times = times; 
		this.timesRow = timesRow;
	}
	
	@Override
	public void p0loadTargets() {
		orderedTargets = new ArrayList<>();
		for (int t = 0; t < multiTargets.size(); t++)
		{
			MultiTarget mt = multiTargets.get(t);
			
			LayerTarget lt = new LayerTarget();
			
			lt.geom = mt.geom;
			lt.rec = mt.rec;
			orderedTargets.add(lt);
		}
	}
	
	public void p2travelTime() {
		if (!analysis().travelTimeRequired()) throw new RuntimeException("dont!");
		for (int t=0;t<orderedTargets.size();t++)
		{
			LayerTarget lt = orderedTargets.get(t);
			lt.time = times[timesRow][t];
		}
	}

}
