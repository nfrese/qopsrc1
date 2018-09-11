package at.qop.qoplib.extinterfaces.batch;

import java.util.ArrayList;
import java.util.Collection;

public class QEXBatchResultSection {
	
	public String catid;
	public String title;
	public double weight;
	public double rating;
	
	public Collection<QEXBatchResultGrp> analyses = new ArrayList<>();


}
