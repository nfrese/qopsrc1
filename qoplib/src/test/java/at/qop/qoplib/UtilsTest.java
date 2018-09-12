package at.qop.qoplib;

import org.junit.Test;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.util.Assert;

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

}
