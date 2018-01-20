package at.qop.qoplib.calculation;

import java.util.ArrayList;
import java.util.List;

public class CalculationSection {

	public List<LayerCalculation> lcs = new ArrayList<>();
	
	public double rating() {
		double _result = 0;
		for (LayerCalculation lc : lcs)
		{
			_result += (lc.rating * lc.weight);
		}
		return _result;
	}
	
	public String getTitle() {

		if (lcs.size() > 0) return lcs.get(0).params.categorytitle;
		
		return null;
	}
}
