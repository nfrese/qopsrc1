package at.qop.qoplib.extinterfaces.json;

import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import at.qop.qoplib.Utils;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileAnalysis;

public class ExportProfile {
	
	public String asJson(Profile profile) throws JsonProcessingException {
		PrettyPrinter pp = new DefaultPrettyPrinter();
		QEXProfile profileBean = map(profile);

		return new ObjectMapper().setDefaultPrettyPrinter(pp).enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(profileBean);
	}

	public QEXProfile map(Profile profile) {
		QEXProfile profileBean = new QEXProfile();

		profileBean.name = profile.name;
		profileBean.description = profile.description;
		profileBean.aggrfn = profile.aggrfn;
		
		for (ProfileAnalysis pa : profile.profileAnalysis.stream().sorted((pa1, pa2) -> Utils.nullSafeCompareTo(pa1.category, pa2.category)).collect(Collectors.toList()))
		{
			QEXProfileAnalysis pab = new QEXProfileAnalysis();
			pab.analysis_name = pa.analysis.name;
			pab.weight= pa.weight;
			pab.altratingfunc = pa.altratingfunc;
			pab.category = pa.category;
			pab.categorytitle = pa.categorytitle;
			pab.ratingvisible = pa.ratingvisible;
			profileBean.profileAnalysis.add(pab);
		}
		return profileBean;
	}

}
