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
