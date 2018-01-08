package at.qop.qoplib.calculation;

import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.dbconnector.DbTableReader;
import at.qop.qoplib.domains.IGenericDomain;
import at.qop.qoplib.entities.ProfileLayer;

public class DbLayerSource implements LayerSource {

	@Override
	public Future<LayerCalculationP1Result> load(Point start, ProfileLayer layerParams) {
		Callable<LayerCalculationP1Result> callable = new Callable<LayerCalculationP1Result>() {

			@Override
			public LayerCalculationP1Result call() throws Exception {
				
				IGenericDomain gd_ = LookupSessionBeans.genericDomain();
				try {
					DbTableReader tableReader = new DbTableReader();
					gd_.readTable(layerParams.query, tableReader);
					LayerCalculationP1Result r = new LayerCalculationP1Result();
					r.table = tableReader.table;
					r.records = tableReader.records;
					return r;
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
			
		};
		FutureTask<LayerCalculationP1Result> ft = new FutureTask<LayerCalculationP1Result>(callable);
		ft.run();
		return ft;
	}

}
