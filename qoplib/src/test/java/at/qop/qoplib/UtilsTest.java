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

import org.junit.Test;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.util.Assert;

public class UtilsTest {

	@Test
	public void parseLonLatStrTest1() {
		Point point = Utils.parseLonLatStr("16.39044652738694, 48.21200859340763");
		Assert.equals("POINT (16.39044652738694 48.21200859340763)", point+"");
	}

	@Test
	public void parseLonLatStrTest2() {
		Point point = Utils.parseLonLatStr("16.39044652738694 48.21200859340763");
		Assert.equals("POINT (16.39044652738694 48.21200859340763)", point+"");
	}

	@Test
	public void parseLonLatStrTest3() {
		Point point = Utils.parseLonLatStr("16.39044652738694  48.21200859340763");
		Assert.equals("POINT (16.39044652738694 48.21200859340763)", point+"");
	}

	@Test
	public void parseLonLatStrTest4() {
		Point point = Utils.parseLonLatStr("16.39044652738694; 48.21200859340763");
		Assert.equals("POINT (16.39044652738694 48.21200859340763)", point+"");
	}

	@Test
	public void parseLonLatStrTest5() {
		Point point = Utils.parseLonLatStr("16,39044652738694; 48,21200859340763");
		Assert.equals("POINT (16.39044652738694 48.21200859340763)", point+"");
	}

	@Test
	public void parseLonLatStrTest6() {
		Point point = Utils.parseLonLatStr("16,39044652738694; 48,21200859340763");
		Assert.equals("POINT (16.39044652738694 48.21200859340763)", point+"");
	}

	@Test
	public void guessTableNameTest1() {
		String result = Utils.guessTableName("select * from autobahnanschluesse LIMIT 1");
		Assert.equals("autobahnanschluesse", result);
	}
	
	@Test
	public void guessTableNameTest2() {
		String result = Utils.guessTableName("select * from auto_bahn_anschluesse");
		Assert.equals("auto_bahn_anschluesse", result);
	}
	
	
	@Test
	public void readResourceToStringTest() {
		String resPath = "/at/qop/qoplib/batch/export_sample1.json";
		String json = Utils.readResourceToString(resPath);
		
		System.out.println(json);
		
	}
}
