/* 
 * Copyright (C) 2018 Norbert Frese
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
*/

package at.qop.qoplib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

import at.qop.qoplib.calculation.CRSTransform;
import at.qop.qoplib.calculation.DbLayerSource.RastTableSQL;

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

	public static String getApplicationName()
	{
		try {
			String appName = System.getenv("qop.application.name");
			if (appName == null) return "qopwebui";
			return appName;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String getEarName()
	{
		Object earName;
		try {
			earName = new InitialContext().lookup("java:app/AppName");
			System.out.println(earName);
			return String.valueOf(earName);
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String guessTableName(String query)
	{
		RastTableSQL rasterTableSql = new RastTableSQL(query);
		if (rasterTableSql.isRasterTable())
		{
			return rasterTableSql.getRasterTablename();
		}
		
		final Pattern p = Pattern.compile("^(.*) from ([a-zA-Z0-9_]+)( .*)?$");
	    Matcher m = p.matcher(query);

	    if (m.find()) {
//	        System.out.println(m.group(0));
//	        System.out.println(m.group(1));
//	        System.out.println(m.group(2));
//	        System.out.println(m.group(3));
	        return m.group(2);
	    }
	    return null;
	}
	
	public static <T extends Comparable<T>> int nullSafeCompareTo(T o1, T o2)
	{
		if (o1 == null || o2 == null) return String.valueOf(o1).compareTo(String.valueOf(o2));
		return o1.compareTo(o2);
	}
	
	public static String readResourceToString(String resPath) {
		InputStream resourceAsStream = Utils.class.getResourceAsStream(resPath);
		if (resourceAsStream == null) throw new RuntimeException("Utils.class.getResourceAsStream(" + resPath +") failed");
		String str = new Scanner(resourceAsStream, "UTF-8").useDelimiter("\\A").next();
		return str;
	}
	
}
