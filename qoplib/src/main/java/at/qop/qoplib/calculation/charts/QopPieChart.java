package at.qop.qoplib.calculation.charts;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class QopPieChart extends QopChart {

	public boolean displayValue = false;
	public boolean displayValueAsInteger = false;
	public boolean displayValueAsPercent = false;
	
	@Override
	public JFreeChart createChart() {
		DefaultPieDataset dataset = new DefaultPieDataset( );
		if (items != null)
		{
			items.entrySet().forEach(
				entry -> { 
					String k = entry.getKey() != null ? entry.getKey() : "";
					double v = entry.getValue();
					String displayValueStr = "";
					if (this.displayValueAsInteger)
					{
						displayValueStr = " (" + ((int)v) + ")";
					}
					else if (this.displayValueAsPercent)
					{
						displayValueStr = " " + (v*100/items.size()) + "%";
					}
					else if (this.displayValue)
					{
						displayValueStr = " (" + v + ")";
					}
					
					dataset.setValue( k  + displayValueStr, entry.getValue()); } 
			);
		}
		
		JFreeChart chart = ChartFactory.createPieChart(      
				this.title,    
				dataset,          
				this.includeLegend,   
				true, 
				false);
		return chart;
	}

	
}
