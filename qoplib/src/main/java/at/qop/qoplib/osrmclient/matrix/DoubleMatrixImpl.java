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
