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

public class DoubleMatrixImpl implements DoubleMatrix {

	private final double[][] arr;
	
	public DoubleMatrixImpl(int rows, int cols)
	{
		arr = new double[rows][cols];
	}
	
	@Override
	public void set(int row, int column, double value) {
		arr[row][column] = value;
	}

	@Override
	public double get(int row, int column) {
		return arr[row][column];
	}

	@Override
	public int rows() {
		return arr.length;
	}

	@Override
	public int columns() {
		if (arr.length > 0)
		{
			return arr[0].length;
		}
		else
		{
			return 0;
		}
	}
	
	public double[][] arr() {
		return arr;
	}
	
	public DoubleMatrix createView(int startRow, int rows, int startCol, int cols)
	{
		return new DoubleMatrixView(startRow, rows, startCol, cols, this);
	}
}
