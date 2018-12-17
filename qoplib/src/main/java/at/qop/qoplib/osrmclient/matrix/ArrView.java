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
