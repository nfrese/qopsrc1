package at.qop.qoplib.calculation;

import java.util.List;

import at.qop.qoplib.entities.Profile;

public class Rating<T extends ILayerCalculation> {
	
	private final Profile profile;
	public final List<CalculationSection<T>> sections;
	public double overallRating;
	
	public Rating(Profile profile, List<CalculationSection<T>> sections) {
		super();
		this.profile = profile;
		this.sections = sections;
	}
	
	public void runRating()
	{
		OverallResult<T> orc;
		if (profile.aggrfn != null && !profile.aggrfn.trim().isEmpty())
		{
			orc = new ScriptedOverallResult<T>(profile.aggrfn, sections);
		}
		else
		{
			orc = new OverallResult<T>(sections);
		}

		orc.run();
		overallRating = orc.overallRating;
	}
}
