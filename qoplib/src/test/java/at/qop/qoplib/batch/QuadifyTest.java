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

package at.qop.qoplib.batch;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;

public class QuadifyTest {

	@Test
	public void testSimple() {
		
		GeometryFactory gf = new GeometryFactory();
		
		QuadifyInMemory<String> testQuadify = new QuadifyInMemory<>(2);
		
		addItem(gf, testQuadify, new Coordinate(1,1), "Berry");
		addItem(gf, testQuadify, new Coordinate(2,2.5), "Else");
		addItem(gf, testQuadify, new Coordinate(3,3), "Latoria");

		testQuadify.run();
		
		Collection<Quadrant> results = testQuadify.listResults();
		
		Assert.assertEquals(4, results.size());
	}
	
	@Test
	public void testAdvanced() {
		
		GeometryFactory gf = new GeometryFactory();
		
		QuadifyInMemory<String> testQuadify = new QuadifyInMemory<>(2);
		
		addItem(gf, testQuadify, new Coordinate(6,5), "Berry");
		addItem(gf, testQuadify, new Coordinate(5,5), "Else");
		addItem(gf, testQuadify, new Coordinate(6,5), "Latoria");
		addItem(gf, testQuadify, new Coordinate(5,6), "Eneida");
		addItem(gf, testQuadify, new Coordinate(1,1), "Tisa");
		addItem(gf, testQuadify, new Coordinate(2,2), "Tandra");
		addItem(gf, testQuadify, new Coordinate(1,5), "Rafael");
		addItem(gf, testQuadify, new Coordinate(5,1), "Pamila");
                                 
		testQuadify.run();
		
		Collection<Quadrant> results = testQuadify.listResults();
		
		Assert.assertEquals(10, results.size());
	}
	
	@Test
	public void testRandom() {
		
		int n=100000;
		int maxPerGroup=1000;
		
		GeometryFactory gf = new GeometryFactory();
		
		QuadifyInMemory<String> testQuadify = new QuadifyInMemory<>(maxPerGroup);
		
		for (int i = 0; i < n; i++)
		{
			addItem(gf, testQuadify, new Coordinate(Math.random(),Math.random()), "Item_" + i);
		}
                                 
		testQuadify.run();
		
		Collection<Quadrant> results = testQuadify.listResults();
		Assert.assertTrue(results.size()<n/2);
		
		Map<Integer, Long> statistics = results.stream().collect(Collectors.groupingBy(q -> q.count, Collectors.counting()));                    // returns a LinkedHashMap, keep order
		System.out.println("#results=" + results.size() + " " + statistics);
		
		long sum = statistics.entrySet().stream().mapToLong(e -> e.getKey()*e.getValue()).sum();
		Assert.assertEquals(n, sum);
		
		long haveChildren = results.stream().filter(q -> q.children != null).count();
		Assert.assertTrue(haveChildren==0);
	}

	private void addItem(GeometryFactory gf, QuadifyInMemory<String> testQuadify, Coordinate coordinate, String name) {
		testQuadify.add(gf.createPoint(coordinate), name + "(" + coordinate.x + "," + coordinate.y + ")");
	}
	
	@Test
	public void testBreakUpEnvelope()
	{
		Envelope envelope = new Envelope(0, 2, 0, 3);
		Envelope[] results = Quadrant.breakUpEnvelope(envelope);
		Assert.assertEquals("[Env[1.0 : 2.0, 1.5 : 3.0], Env[0.0 : 1.0, 1.5 : 3.0], Env[0.0 : 1.0, 0.0 : 1.5], Env[1.0 : 2.0, 0.0 : 1.5]]", Arrays.toString(results));
	}

	@Test
	public void testRightBorderEnvelope() {
		Envelope envelope = new Envelope(0, 2, 0, 3);
		Envelope result = Quadrant.rightBorderEnvelope(envelope);
		Assert.assertEquals("Env[2.0 : 2.0, 0.0 : 3.0]", result + "");
	}
	
	@Test
	public void testBottomBorderEnvelope() {
		Envelope envelope = new Envelope(0, 2, 0, 3);
		Envelope result = Quadrant.bottomBorderEnvelope(envelope);
		Assert.assertEquals("Env[0.0 : 2.0, 0.0 : 0.0]", result + "");
	}
	
	@Test
	public void testBottomRightCornerEnvelope() {
		Envelope envelope = new Envelope(0, 2, 0, 3);
		Envelope result = Quadrant.bottomRightCornerEnvelope(envelope);
		Assert.assertEquals("Env[2.0 : 2.0, 0.0 : 0.0]", result + "");
	}

}
