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

package at.qop.qoplib.osrmclient.matrix;

import org.junit.Test;

import org.junit.Assert;

public class DoubleMatrixTest {

	@Test
	public void test4x3views() {
		DoubleMatrixImpl dm = data4x3();
		
		
		Assert.assertEquals(dm.rows(), 4);
		Assert.assertEquals(dm.columns(), 3);
		
		{
			DoubleMatrix view = dm.createView(2, 2, 1, 2);
			Assert.assertEquals(2, view.rows());
			Assert.assertEquals(2, view.columns());
			
			Assert.assertEquals(700, view.get(0,0), 0.0001);
			Assert.assertEquals(800, view.get(0,1), 0.0001);
			Assert.assertEquals(1000, view.get(1,0), 0.0001);
			Assert.assertEquals(1100, view.get(1,1), 0.0001);
		}

		{
			DoubleMatrix view = dm.createView(0, 4, 0, 3);
			Assert.assertEquals(4, view.rows());
			Assert.assertEquals(3, view.columns());
			
			Assert.assertEquals(100, view.get(0,0), 0.0001);
			Assert.assertEquals(1100, view.get(3,2), 0.0001);
		}
		
		{
			DoubleMatrix view = dm.createView(0, 1, 0, 1);
			Assert.assertEquals(1, view.rows());
			Assert.assertEquals(1, view.columns());
			
			Assert.assertEquals(100, view.get(0,0), 0.0001);
		}
		
		{
			DoubleMatrix view = dm.createView(0, 0, 0, 0);
			Assert.assertEquals(0, view.rows());
			Assert.assertEquals(0, view.columns());
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test4x3fail1() {
		DoubleMatrixImpl dm = data4x3();
		{
			DoubleMatrix view = dm.createView(2, 2, 1, 2);
			view.get(2,0);
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test4x3fail2() {
		DoubleMatrixImpl dm = data4x3();
		{
			DoubleMatrix view = dm.createView(2, 2, 1, 2);
			view.get(0,2);
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void test4x3fail3() {
		DoubleMatrixImpl dm = data4x3();
		{
			DoubleMatrix view = dm.createView(2, 2, 1, 2);
			view.get(-1,0);
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test4x3fail4() {
		DoubleMatrixImpl dm = data4x3();
		{
			DoubleMatrix view = dm.createView(2, 2, 1, 2);
			view.get(0,-1);
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test4x3failCreateView1() {
		DoubleMatrixImpl dm = data4x3();
		{
			@SuppressWarnings("unused")
			DoubleMatrix view = dm.createView(0, 1, 3, 1);
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test4x3failCreateView2() {
		DoubleMatrixImpl dm = data4x3();
		{
			@SuppressWarnings("unused")
			DoubleMatrix view = dm.createView(4, 1, 2, 1);
		}
	}
	
	@Test
	public void test0x0()
	{
		DoubleMatrixImpl dm = new DoubleMatrixImpl(0,0);
		Assert.assertEquals(dm.rows(), 0);
		Assert.assertEquals(dm.columns(), 0);
	}
	
	protected DoubleMatrixImpl data4x3() {
		DoubleMatrixImpl dm = new DoubleMatrixImpl(4,3);
		
		dm.set(0, 0, 100);
		dm.set(0, 1, 200);
		dm.set(0, 2, 300);
		dm.set(1, 0, 300);
		dm.set(1, 1, 400);
		dm.set(1, 2, 500);
		dm.set(2, 0, 600);
		dm.set(2, 1, 700);
		dm.set(2, 2, 800);
		dm.set(3, 0, 900);
		dm.set(3, 1, 1000);
		dm.set(3, 2, 1100);
		return dm;
	}

}
