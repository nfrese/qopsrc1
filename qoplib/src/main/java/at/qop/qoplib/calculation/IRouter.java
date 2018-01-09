package at.qop.qoplib.calculation;

import java.io.IOException;

import at.qop.qoplib.entities.ModeEnum;
import at.qop.qoplib.osrmclient.LonLat;

public interface IRouter {

	double[][] table(ModeEnum mode, LonLat[] sources, LonLat[] destinations) throws IOException;

}
