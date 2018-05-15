package at.qop.qoplib.osrmclient.matrix;

import java.util.stream.Stream;

public interface Arr<T> {

	public void set(int i, T value);
	
	public T get(int i);

	public int length();
	
	public Stream<T> stream();
	
	public Stream<T> stream(int startInclusive, int endExclusive);
	
}
