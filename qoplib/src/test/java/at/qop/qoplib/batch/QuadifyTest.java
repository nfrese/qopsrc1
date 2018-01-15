package at.qop.qoplib.batch;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;

public class QuadifyTest {

	@Test
	public void test() {
		
		GeometryFactory gf = new GeometryFactory();
		
		QuadifyInMemory<String> testQuadify = new QuadifyInMemory<>(2);
		
		addItem(gf, testQuadify,new Coordinate(6,5), "Berry");
		addItem(gf, testQuadify,new Coordinate(5,5), "Else");
		addItem(gf, testQuadify,new Coordinate(6,5), "Latoria");
		addItem(gf, testQuadify,new Coordinate(5,6), "Eneida");
		addItem(gf, testQuadify,new Coordinate(1,1), "Tisa");
		addItem(gf, testQuadify,new Coordinate(2,2), "Tandra");
		addItem(gf, testQuadify,new Coordinate(1,5), "Rafael");
		addItem(gf, testQuadify,new Coordinate(5,1), "Pamila");
//		testQuadify.add(gf.createPoint(new Coordinate(5,5)), "Chantelle");
//		testQuadify.add(gf.createPoint(new Coordinate(5,5)), "Ila");

		testQuadify.run();
		
		Collection<Quadrant> results = testQuadify.listInnerQuadrants();
		
		Assert.assertEquals(10, results.size());
		
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
