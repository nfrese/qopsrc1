package at.qop.qoplib.calculation;

import java.util.concurrent.Future;

import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.entities.LayerParams;

public interface LayerSource {

	public Future<LayerCalculationP1Result> load(Point start, LayerParams layerParams);

}