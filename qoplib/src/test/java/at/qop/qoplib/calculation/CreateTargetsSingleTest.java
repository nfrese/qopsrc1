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

package at.qop.qoplib.calculation;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import at.qop.qoplib.dbconnector.DbRecord;

public class CreateTargetsSingleTest {

	private final class MyCreate extends CreateTargetsSingle {
		@Override
		protected GeometryFactory gf() {
			
			return new GeometryFactory();
		}

		@Override
		protected double len(LineSegment lseg) {
			return lseg.getLength();
		}
	}

	@Test
	public void testSimpleMultiPolygon() throws ParseException {
		CreateTargetsSingle ct = new MyCreate();
		
		Geometry shape = new WKTReader().read("MULTIPOLYGON (((0 0, 1 0, 1 1, 0 1, 0 0)))");
		
		List<LayerTarget> results = new ArrayList<>();
		DbRecord rec = null;
		ct.createTargetsFromRecord(results, rec, shape);
		
		Assert.assertEquals(4, results.size());;
	}

	@Test
	public void testSimplePolygon() throws ParseException {
		CreateTargetsSingle ct = new MyCreate();
		
		Geometry shape = new WKTReader().read("POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))");
		
		List<LayerTarget> results = new ArrayList<>();
		DbRecord rec = null;
		ct.createTargetsFromRecord(results, rec, shape);
		
		Assert.assertEquals(4, results.size());;
	}

	@Test
	public void testSimpleLinestring() throws ParseException {
		CreateTargetsSingle ct = new MyCreate();
		
		Geometry shape = new WKTReader().read("LINESTRING (0 0, 1 0, 1 1, 0 1)");
		
		List<LayerTarget> results = new ArrayList<>();
		DbRecord rec = null;
		ct.createTargetsFromRecord(results, rec, shape);
		
		Assert.assertEquals(4, results.size());;
	}
	
	@Test
	public void testShortLinestring() throws ParseException {
		CreateTargetsSingle ct = new MyCreate();
		
		Geometry shape = new WKTReader().read("LINESTRING (0 0, 1 0)");
		
		List<LayerTarget> results = new ArrayList<>();
		DbRecord rec = null;
		ct.createTargetsFromRecord(results, rec, shape);
		
		Assert.assertEquals(2, results.size());;
	}
	
	@Test
	public void testLongLinestring() throws ParseException {
		CreateTargetsSingle ct = new MyCreate();
		
		Geometry shape = new WKTReader().read("LINESTRING (0 0, 30 0)");
		
		List<LayerTarget> results = new ArrayList<>();
		DbRecord rec = null;
		ct.createTargetsFromRecord(results, rec, shape);
		
		Assert.assertEquals(3, results.size());;
	}
	
	@Test
	public void testSimpleMultiLinestring() throws ParseException {
		CreateTargetsSingle ct = new MyCreate();
		
		Geometry shape = new WKTReader().read("MULTILINESTRING ((0 0, 1 0, 1 1, 0 1))");
		
		List<LayerTarget> results = new ArrayList<>();
		DbRecord rec = null;
		ct.createTargetsFromRecord(results, rec, shape);
		
		Assert.assertEquals(4, results.size());;
	}
	
	
	@Test
	public void testR1030() throws ParseException {
		CreateTargetsSingle ct = new CreateTargetsSingle();
		
		Geometry shape = new WKTReader().read("MULTIPOLYGON (((16.3868546210605 48.2110880498743, 16.3868565976469 48.2110867180113, 16.3868050733468 48.2111092776592, 16.3867475883686 48.2111318309192, 16.3866782067793 48.2111384894605, 16.3858216817965 48.2111825972375, 16.3856749470167 48.2111760326969, 16.3854825911117 48.2111469596954, 16.3854666420701 48.2110581679642, 16.3853318045558 48.2110542508573, 16.3853060436271 48.2110741458015, 16.3853299143888 48.2111456942476, 16.3853597075011 48.2112053241944, 16.385423242261 48.2112887895414, 16.3854609243981 48.2112967233177, 16.3856869963354 48.2113363847538, 16.3858218436021 48.2113495734005, 16.3859903678586 48.2113336003942, 16.3862500977036 48.2113242063294, 16.386325442842 48.211320198425, 16.3864107122324 48.211324136089, 16.3869658589823 48.2112960667073, 16.3869578406204 48.2112019746941, 16.3868982688029 48.2111145334589, 16.3868546210605 48.2110880498743)))");
		
		List<LayerTarget> results = new ArrayList<>();
		DbRecord rec = null;
		ct.createTargetsFromRecord(results, rec, shape);
		
		Assert.assertEquals(shape.getCoordinates().length+2, results.size());;
		Assert.assertEquals(27, results.size());;
	}
}
