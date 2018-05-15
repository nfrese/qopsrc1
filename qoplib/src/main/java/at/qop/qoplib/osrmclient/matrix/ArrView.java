package at.qop.qoplib.osrmclient.matrix;

import java.util.Arrays;
import java.util.stream.Stream;

public class ArrView<T> implements Arr<T> {

	private final Arr<T> parent;
	private final int len;
	private final int start;

	public ArrView(int start, int len, Arr<T> parent) {
		super();
		this.parent = parent;
		this.len = len;
		this.start = start;
		if (start + len > parent.length()) throw new IllegalArgumentException("start + len > parent.lengt");
	}

	@Override
	public void set(int i, T value) {
		checkBounds(i);
		parent.set(start+i, value);
	}

	@Override
	public T get(int i) {
		checkBounds(i);
		return parent.get(start+i);
	}

	private void checkBounds(int i) {
		if (start + i >= parent.length()) throw new IllegalArgumentException("start + i >= parent.lengt");
	}
	
	@Override
	public int length() {
		return len;
	}
	
	@Override
	public Stream<T> stream() {
		return parent.stream(start, start+len);
	}

	@Override
	public Stream<T> stream(int startInclusive, int endExclusive) {
		return parent.stream(start + startInclusive, start + endExclusive);
	}
	
	public int getStart() {
		return start;
	}


}
