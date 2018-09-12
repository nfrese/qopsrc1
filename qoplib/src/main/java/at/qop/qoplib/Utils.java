package at.qop.qoplib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.calculation.CRSTransform;

public class Utils {
	
	@SuppressWarnings("unchecked")
	public static <T> T deepClone(T obj) throws IOException, ClassNotFoundException
	{
		ByteArrayOutputStream baOut = new ByteArrayOutputStream();
		ObjectOutputStream objOut = new ObjectOutputStream(baOut);
		objOut.writeObject(obj);
		objOut.flush();
		objOut.close();
		baOut.close();
		byte[] bytes = baOut.toByteArray();

		ByteArrayInputStream baIn = new ByteArrayInputStream(bytes);
		return (T) new ObjectInputStream(baIn).readObject();		
	}

	public static String uxCmdStringEscape(String name) {
		return "\"" + name.replace("\"", "\\\"") + "\"";
	}

	public static String toPGColumnName(String name) {
		return name.toLowerCase()
				.replace("/", "_")
				.replace("-", "_")
				.replace("&", "_a_")
				.replace(" ", "_")
				.replace("ä", "ae")
				.replace("ö", "oe")
				.replace("ü", "ue")
				.replace("ß", "ss");
	}

	public static Point parseLonLatStr(String lonLatStr) throws IllegalArgumentException {
		
		try {
			String[] splitted = lonLatStr.trim().split(",\\s*|;\\s*|\\s+");

			if (splitted.length == 2)
			{
				return CRSTransform.gfWGS84.createPoint(new Coordinate(
						Double.valueOf(splitted[0]), Double.valueOf(splitted[1])));
			}
			else if (lonLatStr.matches(".*,.*,*")){
				return parseLonLatStr(lonLatStr.replace(",", "."));
			}

		} catch (Exception ex) {
			throw new IllegalArgumentException("Cannot parse " + lonLatStr, ex);
		}
		throw new IllegalArgumentException("Cannot parse " + lonLatStr);
	}
	
}
