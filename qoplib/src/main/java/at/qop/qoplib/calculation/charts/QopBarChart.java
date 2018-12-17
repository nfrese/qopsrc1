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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class QopBarChart extends QopChart {

	public boolean displayValue = false;
	public boolean displayValueAsInteger = false;
	public boolean displayValueAsPercent = false;
	private String categoryAxisLabel;
	private String valueAxisLabel;
	
	@Override
	public JFreeChart createChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
		if (items != null)
		{
			items.entrySet().forEach(
				entry -> { 
					String k = entry.getKey() != null ? entry.getKey() : "";
					double v = entry.getValue();
					
					String[] parts = k.split("\\|",2);
					if (parts.length == 2)
					{
						dataset.addValue( entry.getValue(), parts[0], parts[1]); 
					}	
					else if (parts.length == 1)
					{
						dataset.addValue( entry.getValue(), "", parts[0]); 
					}
					
				}	
			);
		}
		
        JFreeChart chart = ChartFactory.createBarChart(
        		this.title,       
                this.categoryAxisLabel,               
                this.valueAxisLabel,                  
                dataset,                  
                PlotOrientation.VERTICAL, 
                this.includeLegend,       
                true,                     
                false                     
            );
		return chart;
	}

	
}
