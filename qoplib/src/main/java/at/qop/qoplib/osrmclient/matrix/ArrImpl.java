package at.qop.qoplib.osrmclient.matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ArrImpl<T> implements Arr<T> {

	private final T[] arr;
	
	public ArrImpl(T[] arr) {
		super();
		this.arr = arr;
	}

	@Override
	public void set(int i, T value) {
		arr[i] = value;
	}

	@Override
	public T get(int i) {
		return arr[i];
	}

	@Override
	public int length() {
		return arr.length;
	}

	public ArrView<T> createView(int start, int len)
	{
		return new ArrView<T>(start, len, this);
	}
	
	public List<ArrView<T>> views(int maxSize)
	{
		List<ArrView<T>> results = new ArrayList<>();
		
		for (int start=0; start<arr.length; start += maxSize)
		{
			int l = Math.min(maxSize, arr.length-start);
			if (l > 0)
			{
				results.add(createView(start, l));
			}
		}
		return results;
	}

	@Override
	public Stream<T> stream() {
		return Arrays.stream(arr);
	}

	@Override
	public Stream<T> stream(int startInclusive, int endExclusive) {
		return Arrays.stream(arr, startInclusive, endExclusive);
	}
	
}
