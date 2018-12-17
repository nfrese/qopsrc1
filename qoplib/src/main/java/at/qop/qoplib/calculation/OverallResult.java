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

import java.util.List;

public class OverallResult<T extends ILayerCalculation> {
	
	public final List<CalculationSection<T>> sections;
	public double overallRating;
	
	public OverallResult(List<CalculationSection<T>> sections) {
		super();
		this.sections = sections;
	}
	
	public void run() {
		double overallSumRating = 0;
		double overallSumWeight = 0;
		
		for (CalculationSection<T> section : sections)
		{
			if (section.lcs.size() > 0)
			{
				double sectionSumWeight = 0;
				double sectionSumRating = 0;
				for (ILayerCalculation lc : section.lcs)
				{
					if (Double.isNaN(lc.getRating()))
							{
						System.out.println();
							}
					
					sectionSumRating += (lc.getRating() * lc.getWeight());
					sectionSumWeight += lc.getWeight(); 
				}
				
				if (sectionSumWeight > 0)
				{
					section.rating = sectionSumRating / sectionSumWeight;
				}
				else
				{
					section.rating = 0;
				}
				section.weight = sectionSumWeight;
			}
			else
			{
				section.rating = 0;
				section.weight = 0;
			}
			
			overallSumRating += (section.rating * section.weight);
			overallSumWeight += section.weight;
		}
		if (overallSumWeight > 0)
		{
			overallRating = overallSumRating / overallSumWeight;
		}
		else
		{
			overallRating = 0;
		}
	}

}
