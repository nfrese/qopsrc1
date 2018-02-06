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
