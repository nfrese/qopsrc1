package at.qop.qoplib.batch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.strtree.STRtree;

import at.qop.qoplib.ConfigFile;
import at.qop.qoplib.Constants;
import at.qop.qoplib.batch.WriteBatTable.BatRecord;
import at.qop.qoplib.batch.WriteBatTable.ColGrp;
import at.qop.qoplib.calculation.CRSTransform;
import at.qop.qoplib.calculation.CreateTargetsMulti;
import at.qop.qoplib.calculation.DbLayerSource;
import at.qop.qoplib.calculation.IRouter;
import at.qop.qoplib.calculation.LayerCalculation;
import at.qop.qoplib.calculation.LayerCalculationP1Result;
import at.qop.qoplib.calculation.LayerSource;
import at.qop.qoplib.calculation.MultiTarget;
import at.qop.qoplib.calculation.multi.LayerCalculationMultiEuclidean;
import at.qop.qoplib.calculation.multi.LayerCalculationMultiSimple;
import at.qop.qoplib.calculation.multi.LayerCalculationMultiTT;
import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.fieldtypes.DbGeometryField;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Analysis;
import at.qop.qoplib.entities.ModeEnum;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileAnalysis;
import at.qop.qoplib.osrmclient.LonLat;
import at.qop.qoplib.osrmclient.OSRMClient;

public abstract class BatchCalculationAbstract implements Runnable {

	private static final CreateTargetsMulti CREATE_TARGETS_MULTI = new CreateTargetsMulti();
	protected static final int maxPerRect = 3000;
	protected final Profile currentProfile;
	private LayerSource source;
	private ConfigFile cf;
	private IRouter router;
	public int overall = -1;
	public int count = 0;
	public boolean cancelled = false;

	public BatchCalculationAbstract(Profile currentProfile) {
		this.currentProfile = currentProfile;

		source = new DbLayerSource();
		cf = ConfigFile.read();
		router = new OSRMClient(cf.getOSRMHost(), cf.getOSRMPort(), Constants.SPLIT_DESTINATIONS_AT);
	}
	
	protected abstract void initOutput();

	protected abstract Quadify initQuadify();

	protected abstract List<Address> addressesForQuadrant(Envelope envelope);

	protected abstract void outputRecs(BatRecord[] batRecs);
	
	protected abstract void outputDone();

	protected abstract void failed(Throwable t);
	
	protected void progress(int overall_, int count_) {
		int percent = (100* count)/overall_;
		System.out.println("Progress: " + count_ + "/" + overall_ + " = " + percent + "%");
	}


	protected void success() {
	}

	
	public void run()
	{
		try {
			runInternal();
			success();
		}
		catch (Throwable t)
		{
			failed(t);
		}
	}
	
	private void runInternal()
	{
		initOutput();

		Quadify quadify = initQuadify();
		quadify.run();
		overall = quadify.getOverall();
		
		Collection<Quadrant> results = quadify.listResults();

		Map<Integer, Long> statistics = results.stream().collect(Collectors.groupingBy(q -> q.count, Collectors.counting()));                    // returns a LinkedHashMap, keep order

		long sum = statistics.entrySet().stream().mapToLong(e -> e.getKey()*e.getValue()).sum();
		System.out.println("sum=" + sum);

		for (Quadrant result : results)
		{
			Envelope envelope = result.envelope;
			List<Address> addresses = addressesForQuadrant(envelope);
			doCalculation(result, currentProfile, addresses, source, router);
			
			
			if (addresses.size() != result.count)
			{
				System.out.println("found:" + addresses.size() + " != expected: " + result.count);
				if (addresses.size() < result.count) throw new RuntimeException("found:" + addresses.size() + " < expected: " + result.count);
			}
		}
		outputDone();
	}

	private void doCalculation(Quadrant result, Profile profile, List<Address> addresses, LayerSource source2,
			IRouter router2) {
		
		if (addresses.size() == 0) return;
		
		BatRecord[] batRecs = initBatRecs(profile, addresses);
		
		Geometry start = CRSTransform.gfWGS84.toGeometry(result.envelope);
		
		int profileCnt = 0;
		for (ProfileAnalysis profileAnalysis : profile.profileAnalysis) {	
			
			if (cancelled) throw new CancellationException();
			
			Analysis params = profileAnalysis.analysis;
			LayerCalculationP1Result loaded = source.load(start, params);

			DbGeometryField geomField = loaded.table.field(params.geomfield, DbGeometryField.class);

			ArrayList<MultiTarget> multiTargets = new ArrayList<>();
			
			for (DbRecord target : loaded.records)
			{
				Geometry geom = geomField.get(target);
				CREATE_TARGETS_MULTI.createTargetsFromRecord(multiTargets, target, geom);
			}

			if (profileAnalysis.analysis.travelTimeRequired() )
			{
				double[][] times = null;
				LonLat[] sources = lonLatFromAddresses(addresses);
				times = p2travelTime(sources, multiTargets, profileAnalysis.analysis.mode);
				System.out.println("LayerCalculation (with matrix)");
				
				for (int i = 0; i < addresses.size(); i++)
				{
					Address address = addresses.get(i);
					
					LayerCalculationMultiTT lc = new LayerCalculationMultiTT(address.geom, profileAnalysis, 
							profileAnalysis.weight, profileAnalysis.altratingfunc, loaded.table, multiTargets, times, i);
					performCalculation(batRecs[i], profileCnt, lc);
				}
			}
			else if (profileAnalysis.analysis.hasRadius())
			{
				System.out.println("LayerCalculation (with radius)");
				
				STRtree spatIx = new STRtree();
				for (MultiTarget mt : multiTargets)
				{
					spatIx.insert(mt.geom.getEnvelopeInternal(), mt);
				}
				
				for (int i = 0; i < addresses.size(); i++)
				{
					Address address = addresses.get(i);
					
					LayerCalculationMultiEuclidean lc = new LayerCalculationMultiEuclidean(address.geom, profileAnalysis, 
							profileAnalysis.weight, profileAnalysis.altratingfunc, loaded.table, spatIx);
					performCalculation(batRecs[i], profileCnt, lc);
				}
			}
			else 
			{
				System.out.println("LayerCalculation (simple)");
				
				for (int i = 0; i < addresses.size(); i++)
				{
					Address address = addresses.get(i);
					
					LayerCalculationMultiSimple lc = new LayerCalculationMultiSimple(address.geom, profileAnalysis, 
							profileAnalysis.weight, profileAnalysis.altratingfunc, loaded.table, multiTargets);
					performCalculation(batRecs[i], profileCnt, lc);
				}
				
			}
			profileCnt++;

		}
		
		outputRecs(batRecs);
		
		count += addresses.size();
		progress(overall, count);
	}

	public void performCalculation(BatRecord batRec, int profileCnt, LayerCalculation lc) {
		lc.p0loadTargets();
		lc.p1calcDistances();
		lc.p2travelTime();
		lc.p3OrderTargets();
		lc.p4Calculate();
		ColGrp g = new ColGrp();
		g.name = lc.analysis().batColumnName();
		g.result = lc.result;
		g.rating = lc.rating;
		g.pa = lc.getParams();
		batRec.colGrps[profileCnt] = g; 
	}

	private BatRecord[] initBatRecs(Profile profile, List<Address> addresses) {
		BatRecord[] batRecs = new BatRecord[addresses.size()];
		for (int i = 0; i < addresses.size(); i++) {
			Address addr = addresses.get(i);
			BatRecord batRec = new BatRecord(profile.profileAnalysis.size());
			batRec.name = addr.name;
			batRec.geom = addr.geom;
			batRecs[i] = batRec;
		}
		return batRecs;
	}
	
	private LonLat[] lonLatFromAddresses(List<Address> addresses) {
		LonLat[] sources = new LonLat[addresses.size()];
		for (int i = 0; i < addresses.size(); i++)
		{
			Coordinate c = addresses.get(i).geom.getCoordinate();
			sources[i] = new LonLat(c.x, c.y);
		}
		return sources;
	}

	public double[][] p2travelTime(LonLat[] sources, ArrayList<MultiTarget> orderedTargets, ModeEnum mode) {

		LonLat[] destinations = new LonLat[orderedTargets.size()];
		for (int i = 0; i < orderedTargets.size(); i++)
		{
			Coordinate c = orderedTargets.get(i).geom.getCoordinate();
			destinations[i] = new LonLat(c.x, c.y);
		}

		try {
			double[][] r = router.table(mode, sources, destinations);
			double time[][] = new double[r.length][r[0].length];
			for (int j=0; j < sources.length; j++)
			{
				for (int i = 0; i < orderedTargets.size(); i++) {

					double timeMinutes = r[j][i] / 60;  // minutes
					time[j][i] = ((double)Math.round(timeMinutes * 100)) / 100;  // round 2 decimal places
				}
			}
			return time;
		} catch (IOException e) {
			throw new RuntimeException(e); 
		}
	}
}
