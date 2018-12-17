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

package at.qop.qoplib.calculation.charts;

import java.util.LinkedHashMap;

import org.jfree.chart.JFreeChart;

public abstract class QopChart {
	
	public String title = null;
	
	public boolean includeLegend = false;
	
	public LinkedHashMap<String, Double> items;

	public void put(String label, double value)
	{
		if (items == null) items = new LinkedHashMap<>();
		items.put(label, value);
	}

	public void increment(String label)
	{
		add(label, 1);
	}
	
	public void add(String label, double value)
	{
		if (items == null) items = new LinkedHashMap<>();
		Double prevVal = items.get(label);
		if (prevVal != null)
		{
			items.put(label, prevVal + value);
		}
		else
		{
			items.put(label, value);
		}
	}
	
	public abstract JFreeChart createChart();
	
}
