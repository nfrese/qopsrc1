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
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

public class SectionBuilder<T extends ISectionBuilderInput> {

	private final Collection<T> layerCalculations;
	
	public SectionBuilder(Collection<T> layerCalculations) {
		super();
		this.layerCalculations = layerCalculations;
	}

	public ArrayList<CalculationSection<T>> run()
	{
		Comparator<T> comparator = Comparator.comparing(lc -> lc.getParams().category + "");
		comparator = comparator.thenComparing(Comparator.comparing(lc -> lc.getParams().analysis.name + ""));

		T lastLc = null;

		CalculationSection<T> current = null;
		ArrayList<CalculationSection<T>> sections = new ArrayList<>();

		for (T lc : layerCalculations.stream()
				.sorted(comparator)
				.collect(Collectors.toList()))
		{

			if (nextCategory(lastLc, lc))
			{
				if (current != null)
				{
					sections.add(current);
				}
				current = new CalculationSection<T>();
			}
			lastLc = lc; current.lcs.add(lc);
		};
		
		if (current != null)
		{
			sections.add(current);
		}
		return sections;
	}
	
	
	private boolean nextCategory(T lastLc, T lc) {
		if (lastLc == null)	return true;

		String lastCat = lastLc.getParams().category;
		String cat = lc.getParams().category;

		if (lastCat == null && cat == null) return false;
		if (lastCat == null) return true;

		String[] lastCatSplit = lastCat.split("\\.");
		String[] catSplit = {};
		if (cat != null) catSplit = cat.split("\\.");

		if (lastCatSplit.length != catSplit.length) return true;

		int changeIx = lastCatSplit.length;

		for (int i = 0; i < lastCatSplit.length; i++)
		{
			if (!catSplit[i].equals(lastCatSplit[i]))
			{
				changeIx = i;
				break;
			}
		}

		if (changeIx < lastCatSplit.length -1)
		{
			return true;
		}
		return false;
	}
}
