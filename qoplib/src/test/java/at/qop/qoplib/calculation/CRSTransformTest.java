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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class CRSTransformTest {

	@Test
	public void testFromWGS84() {
		CRSTransform crst = CRSTransform.singleton;
		Point p84 = CRSTransform.gfWGS84.createPoint(new Coordinate(16.369561009437817, 48.20423271310815));
		Point p31256 = (Point)crst.fromWGS84(p84);
		
		assertEquals(31256, p31256.getSRID());
		assertEquals("POINT (2781.9335246643677 340648.0859658886)", p31256+"");
	}
	
	@Test
	public void testToWGS84() {
		CRSTransform crst = CRSTransform.singleton;
		Point p31256 = CRSTransform.gf31256.createPoint(new Coordinate(2781.9335246643677, 340648.0859658886));
		Point p84 = null;
		for (int i=0;i<100000;i++)
			p84 = (Point)crst.toWGS84(p31256);
		
		assertEquals(4326, p84.getSRID());
		assertEquals("POINT (16.369561016493716 48.204232797698445)", p84+"");
	}
	
	@Test
	public void testDistanceWGS84() {
		CRSTransform crst = CRSTransform.singleton;
		Point p1 = CRSTransform.gfWGS84.createPoint(new Coordinate(16.369561009437817, 48.20423271310815));
		Point p2 = CRSTransform.gfWGS84.createPoint(new Coordinate(16.37741831002266, 48.20776186641345));
		
		double d = crst.distanceWGS84(p1, p2);
		
		assertEquals(703.61, d, 0.01);
	}
	
	@Test
	public void testBufferWGS84() {
		CRSTransform crst = CRSTransform.singleton;
		Point p1 = CRSTransform.gfWGS84.createPoint(new Coordinate(16.369561009437817, 48.20423271310815));
		Point p2 = CRSTransform.gfWGS84.createPoint(new Coordinate(16.37741831002266, 48.20776186641345));
		
		double d = crst.distanceWGS84(p1, p2);
		double something = (d / 50);
		
		Geometry buffered = crst.bufferWGS84Corr(p1, d+ something);
		
		assertTrue(p2.intersects(buffered));
		
		Geometry bufferedSmall = crst.bufferWGS84Corr(p1, d-something);
		
		assertFalse(p2.intersects(bufferedSmall));
	}

}
