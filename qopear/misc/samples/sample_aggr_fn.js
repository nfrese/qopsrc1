var overallSumRating = 0;
var overallSumWeight = 0;	
for each(var section in ca.sections)
{
	if (section.lcs.size() > 0)
	{
		var sectionSumWeight = 0;
		var sectionSumRating = 0;
		for each (var lc in section.lcs)
		{
			sectionSumRating += (lc.rating * lc.weight);
			sectionSumWeight += lc.weight; 
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
	ca.overallRating = overallSumRating / overallSumWeight;
}
else
{
	ca.overallRating = 0;
}