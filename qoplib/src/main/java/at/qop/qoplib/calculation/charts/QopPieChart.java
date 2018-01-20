package at.qop.qoplib.calculation.charts;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class QopPieChart extends QopChart {

	@Override
	public JFreeChart createChart() {
		DefaultPieDataset dataset = new DefaultPieDataset( );
		if (items != null)
		{
			items.entrySet().forEach(
				entry -> { 
					String k = entry.getKey() != null ? entry.getKey() : "";
					dataset.setValue( k  + " (" + ((int)(double)entry.getValue()) + ")", entry.getValue()); } 
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
