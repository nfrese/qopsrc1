package at.qop.qoplib.extinterfaces.batch;

import java.util.ArrayList;
import java.util.Collection;

public class QEXBatchResult {
	
	public int id;
	public String name;
	public double lat;
	public double lon;
	public Collection<QEXBatchResultGrp> grps = new ArrayList<>();
	

}
