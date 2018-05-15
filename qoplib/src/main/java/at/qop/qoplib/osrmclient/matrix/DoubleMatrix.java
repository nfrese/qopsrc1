package at.qop.qoplib.osrmclient.matrix;

public interface DoubleMatrix {
	
	public void set(int row, int column, double value);
	
	public double get(int row, int column);

	public int rows();
	
	public int columns();
	
}
