package at.qop.qoplib.calculation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
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

}
