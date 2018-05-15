package at.qop.qoplib.osrmclient.matrix;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ArrTest {

	@Test
	public void test() {
		
		Integer[] arr = new Integer[] {100,200,300,400,500};  
		
		ArrImpl<Integer> impl = new ArrImpl<>(arr);
		
		Assert.assertEquals(500, impl.get(4), 0.0001);
		Assert.assertEquals(5, impl.length());
		
		{
			ArrView<Integer> view = impl.createView(3, 2);

			Assert.assertEquals(2, view.length());
			Assert.assertEquals(400, view.get(0), 0.0001);
			Assert.assertEquals(500, view.get(1), 0.0001);
			
			// streams
			
			Assert.assertEquals(2, view.stream().count());
			Assert.assertEquals(400, view.stream(0,1).findFirst().get(), 0.0001);
			Assert.assertEquals(500, view.stream(1,2).findFirst().get(), 0.0001);
		}
		
		{
			List<ArrView<Integer>> views = impl.views(1);
			Assert.assertEquals(5, views.size());
			
			Assert.assertEquals(1, views.get(0).length());
			Assert.assertEquals(1, views.get(1).length());
			Assert.assertEquals(1, views.get(2).length());
			Assert.assertEquals(1, views.get(3).length());
			Assert.assertEquals(1, views.get(4).length());
			
			Assert.assertEquals(100, views.get(0).get(0), 0.0001);
			Assert.assertEquals(200, views.get(1).get(0), 0.0001);
			Assert.assertEquals(300, views.get(2).get(0), 0.0001);
			Assert.assertEquals(400, views.get(3).get(0), 0.0001);
			Assert.assertEquals(500, views.get(4).get(0), 0.0001);
			
			// streams
			
			Assert.assertEquals(1, views.get(0).stream().count());
			Assert.assertEquals(1, views.get(1).stream().count());
			Assert.assertEquals(1, views.get(2).stream().count());
			Assert.assertEquals(1, views.get(3).stream().count());
			Assert.assertEquals(1, views.get(4).stream().count());

			Assert.assertEquals(100, views.get(0).stream().findFirst().get(), 0.0001);
			Assert.assertEquals(200, views.get(1).stream().findFirst().get(), 0.0001);
			Assert.assertEquals(300, views.get(2).stream().findFirst().get(), 0.0001);
			Assert.assertEquals(400, views.get(3).stream().findFirst().get(), 0.0001);
			Assert.assertEquals(500, views.get(4).stream(0,1).findFirst().get(), 0.0001);
		}
		
		{
			List<ArrView<Integer>> views = impl.views(3);
			Assert.assertEquals(2, views.size());
			
			Assert.assertEquals(3, views.get(0).length());
			Assert.assertEquals(2, views.get(1).length());
			
			Assert.assertEquals(100, views.get(0).get(0), 0.0001);
			Assert.assertEquals(200, views.get(0).get(1), 0.0001);
			Assert.assertEquals(300, views.get(0).get(2), 0.0001);
			Assert.assertEquals(400, views.get(1).get(0), 0.0001);
			Assert.assertEquals(500, views.get(1).get(1), 0.0001);
			
			// streams
			
			Assert.assertEquals(3, views.get(0).stream().count());
			Assert.assertEquals(2, views.get(1).stream().count());
		}
		
		{
			List<ArrView<Integer>> views = impl.views(5);
			Assert.assertEquals(1, views.size());
			
			Assert.assertEquals(5, views.get(0).length());
			
			Assert.assertEquals(100, views.get(0).get(0), 0.0001);
			Assert.assertEquals(200, views.get(0).get(1), 0.0001);
			Assert.assertEquals(300, views.get(0).get(2), 0.0001);
			Assert.assertEquals(400, views.get(0).get(3), 0.0001);
			Assert.assertEquals(500, views.get(0).get(4), 0.0001);
		}
		
		{
			List<ArrView<Integer>> views = impl.views(500);
			Assert.assertEquals(1, views.size());
			
			Assert.assertEquals(5, views.get(0).length());
			
			Assert.assertEquals(100, views.get(0).get(0), 0.0001);
			Assert.assertEquals(200, views.get(0).get(1), 0.0001);
			Assert.assertEquals(300, views.get(0).get(2), 0.0001);
			Assert.assertEquals(400, views.get(0).get(3), 0.0001);
			Assert.assertEquals(500, views.get(0).get(4), 0.0001);
			
			// streams
			
			Assert.assertEquals(1500, views.get(0).stream().mapToDouble(x -> x).sum(), 0.0001);
			Assert.assertEquals(900, views.get(0).stream(3,5).mapToDouble(x -> x).sum(), 0.0001);
		}

	}

}
