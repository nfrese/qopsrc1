package at.qop.qoplib.osrmclient.matrix;

public class DoubleMatrixView implements DoubleMatrix {
	
	private final int startRow; 
	private final int rows;
	private final int startCol;
	private final int cols;
	
	private final DoubleMatrix parent;
	
	public DoubleMatrixView(int startRow, int rows, int startCol, int cols, DoubleMatrix parent) {
		super();
		this.startRow = startRow;
		this.rows = rows;
		this.startCol = startCol;
		this.cols = cols;
		this.parent = parent;
		
		if (startRow + rows > parent.rows()) throw new IllegalArgumentException("startRow + rows > parent.rows");
		if (startCol + cols > parent.columns()) throw new IllegalArgumentException("startCol + cols > parent.columns");
	}

	@Override
	public void set(int row, int column, double value) {
		checkBounds(row, column);
		parent.set(startRow + row, startCol + column, value);
	}

	@Override
	public double get(int row, int column) {
		checkBounds(row, column);		
		return parent.get(startRow + row, startCol + column);
	}

	private void checkBounds(int row, int column) {
		if (row < 0) throw new IllegalArgumentException("row < 0");
		if (column < 0) throw new IllegalArgumentException("column < 0");
		if (row >= rows) throw new IllegalArgumentException("row >= rows");
		if (column >= cols) throw new IllegalArgumentException("column >= cols");
	}
	
	@Override
	public int rows() {
		return rows;
	}

	@Override
	public int columns() {
		return cols;
	}
}
