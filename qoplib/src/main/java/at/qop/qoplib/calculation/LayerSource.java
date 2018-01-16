package at.qop.qoplib.calculation;

import java.util.concurrent.Future;

import com.vividsolutions.jts.geom.Geometry;

public interface LayerSource {

	LayerCalculationP1Result load(Geometry start, ILayerCalculationP1Params layerParams);

}
