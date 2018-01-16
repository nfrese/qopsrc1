package at.qop.qoplib.batch;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.strtree.STRtree;

import at.qop.qoplib.ConfigFile;
import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.calculation.CRSTransform;
import at.qop.qoplib.calculation.DbLayerSource;
import at.qop.qoplib.calculation.IRouter;
import at.qop.qoplib.calculation.LayerCalculation;
import at.qop.qoplib.calculation.LayerCalculationP1Result;
import at.qop.qoplib.calculation.LayerSource;
import at.qop.qoplib.calculation.MultiTarget;
import at.qop.qoplib.calculation.multi.LayerCalculationMultiEuclidean;
import at.qop.qoplib.calculation.multi.LayerCalculationMultiSimple;
import at.qop.qoplib.calculation.multi.LayerCalculationMultiTT;
import at.qop.qoplib.dbconnector.AbstractDbTableReader;
import at.qop.qoplib.dbconnector.DBUtils;
import at.qop.qoplib.dbconnector.DbRecord;
import at.qop.qoplib.dbconnector.DbTable;
import at.qop.qoplib.dbconnector.fieldtypes.DbGeometryField;
import at.qop.qoplib.dbconnector.fieldtypes.DbTextField;
import at.qop.qoplib.domains.IGenericDomain;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Analysis;
import at.qop.qoplib.entities.ModeEnum;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.entities.ProfileAnalysis;
import at.qop.qoplib.osrmclient.LonLat;
import at.qop.qoplib.osrmclient.OSRMClient;

public class BatchCalculation implements Runnable {

	private final Profile currentProfile;
	private LayerSource source;
	private ConfigFile cf;
	private IRouter router;
	public int overall = -1;
	public int count = 0;
	public boolean cancelled = false;

	public BatchCalculation(Profile currentProfile) {
		this.currentProfile = currentProfile;

		source = new DbLayerSource();
		cf = ConfigFile.read();
		router = new OSRMClient(cf.getOSRMHost(), cf.getOSRMPort());
	}

	protected void progress(int overall_, int count_) {
		int percent = (100* count)/overall_;
		System.out.println("Progress: " + count_ + "/" + overall_ + " = " + percent + "%");
	}

	protected void failed(Throwable t) {
		t.printStackTrace();
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
		String geomField = "geom";
		QuadifyImpl quadify = new QuadifyImpl(3000, Address.TABLENAME, geomField);
		quadify.run();
		overall = quadify.getOverall();
		
		Collection<Quadrant> results = quadify.listResults();

		Map<Integer, Long> statistics = results.stream().collect(Collectors.groupingBy(q -> q.count, Collectors.counting()));                    // returns a LinkedHashMap, keep order
		//System.out.println("#results=" + results.size() + " " + statistics);

		long sum = statistics.entrySet().stream().mapToLong(e -> e.getKey()*e.getValue()).sum();
		System.out.println("sum=" + sum);

		for (Quadrant result : results)
		{
			String sql = "select * from " + Address.TABLENAME
					+ " WHERE " + geomField 
					+ " && " + DBUtils.stMakeEnvelope(result.envelope);
			IGenericDomain gd_ = LookupSessionBeans.genericDomain();

			List<Address> addresses = new ArrayList<>();
			
			AbstractDbTableReader tableReader = new AbstractDbTableReader() {

				DbTable table;
				private DbGeometryField geomField;
				private DbTextField nameField;

				@Override
				public void metadata(DbTable table) {
					this.table = table;
					geomField = table.geometryField("geom");
					nameField = table.textField("name");
				}

				@Override
				public void record(DbRecord record) {

					Geometry geom = geomField.get(record);
					String name = nameField.get(record);
					//System.out.println(geom + " - " + name);

					Address currentAddress = new Address();
					currentAddress.geom = (Point)geom;
					currentAddress.name = name;
					addresses.add(currentAddress);
				}

			};
			try {
				gd_.readTable(sql, tableReader);
				doCalculation(result, currentProfile, addresses, source, router);
				
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}		
		}

	}
	
	private void doCalculation(Quadrant result, Profile profile, List<Address> addresses, LayerSource source2,
			IRouter router2) {
		
		if (addresses.size() == 0) return;
		
		Geometry start = CRSTransform.gfWGS84.toGeometry(result.envelope);

		for (ProfileAnalysis profileAnalysis : profile.profileAnalysis) {	
			
			if (cancelled) throw new CancellationException();
			
			Analysis params = profileAnalysis.analysis;
			LayerCalculationP1Result loaded = source.load(start, params);

			DbGeometryField geomField = loaded.table.field(params.geomfield, DbGeometryField.class);

			ArrayList<MultiTarget> multiTargets = new ArrayList<>();
			for (DbRecord target : loaded.records)
			{
				MultiTarget lt = new MultiTarget();

				lt.geom = geomField.get(target);

				lt.rec = target;
				multiTargets.add(lt);
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
					
					LayerCalculationMultiTT lc = new LayerCalculationMultiTT(address.geom, params, 
							profileAnalysis.weight, profileAnalysis.altratingfunc, loaded.table, multiTargets, times, i);
					performCalculation(lc);
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
					
					LayerCalculationMultiEuclidean lc = new LayerCalculationMultiEuclidean(address.geom, params, 
							profileAnalysis.weight, profileAnalysis.altratingfunc, loaded.table, spatIx);
					performCalculation(lc);
				}
			}
			else 
			{
				System.out.println("LayerCalculation (simple)");
				
				for (int i = 0; i < addresses.size(); i++)
				{
					Address address = addresses.get(i);
					
					LayerCalculationMultiSimple lc = new LayerCalculationMultiSimple(address.geom, params, 
							profileAnalysis.weight, profileAnalysis.altratingfunc, loaded.table, multiTargets);
					performCalculation(lc);
				}
				
			}

		}
		count += addresses.size();
		progress(overall, count);
	}

	public void performCalculation(LayerCalculation lc) {
		lc.p0loadTargets();
		lc.p1calcDistances();
		lc.p2travelTime();
		lc.p3OrderTargets();
		lc.p4Calculate();
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
