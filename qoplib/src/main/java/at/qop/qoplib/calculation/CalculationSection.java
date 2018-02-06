package at.qop.qoplib.calculation;

import java.util.ArrayList;
import java.util.List;

import at.qop.qoplib.Utils;

public class CalculationSection<L extends ISectionBuilderInput> {

	public List<L> lcs = new ArrayList<>();
	public double rating;
	public double weight;
	
	public String getTitle() {

		if (lcs.size() > 0) return lcs.get(0).getParams().categorytitle;
		
		return null;
	}
	
	public String getSectionColumnName()
	{
		String title = getTitle();
		if (title == null) return "s_notitle";
		return "s_" + Utils.toPGColumnName(title);
	}
}
