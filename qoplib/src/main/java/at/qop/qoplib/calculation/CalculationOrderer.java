package at.qop.qoplib.calculation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class CalculationOrderer {

	private final Calculation calculation;
	
	public CalculationOrderer(Calculation calculation) {
		super();
		this.calculation = calculation;
	}

	public void run()
	{
		Comparator<LayerCalculation> comparator = Comparator.comparing(lc -> lc.params.category + "");
		comparator = comparator.thenComparing(Comparator.comparing(lc -> lc.params.analysis.name + ""));

		LayerCalculation lastLc = null;

		CalculationSection current = null;
		calculation.sections = new ArrayList<>();

		for (LayerCalculation lc : calculation.layerCalculations.stream()
				.sorted(comparator)
				.collect(Collectors.toList()))
		{

			if (nextCategory(lastLc, lc))
			{
				if (current != null)
				{
					renderSectionSums(current);
					calculation.addSection(current);
				}

				current = new CalculationSection();

				renderTitles(lc);

				renderHeaders(lc);
			}
			lastLc = lc; current.lcs.add(lc);

			renderAnalysis(lc);
		};
		
		if (current != null)
		{
			renderSectionSums(current);
			calculation.addSection(current);
		}

		refreshOverallRating(calculation);
	}


	protected void renderAnalysis(LayerCalculation lc) {
	}


	protected void renderHeaders(LayerCalculation lc) {
	}


	protected void renderTitles(LayerCalculation lc) {
	}


	protected void renderSectionSums(CalculationSection current) {
	}


	protected void refreshOverallRating(Calculation calculation2) {
	}
	
	
	private boolean nextCategory(LayerCalculation lastLc, LayerCalculation lc) {
		if (lastLc == null)	return true;

		String lastCat = lastLc.params.category;
		String cat = lc.params.category;

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
