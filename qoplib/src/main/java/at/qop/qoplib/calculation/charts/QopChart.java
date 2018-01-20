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
