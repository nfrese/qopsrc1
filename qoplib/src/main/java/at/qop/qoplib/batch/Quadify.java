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

package at.qop.qoplib.batch;

import java.util.ArrayList;
import java.util.Collection;
import com.vividsolutions.jts.geom.Envelope;

public abstract class Quadify {
	
	public final int maxPerRect;
	
	public Quadrant root = null;
	
	public Quadify(int maxPerRect) {
		super();
		this.maxPerRect = maxPerRect;
	}
	
	protected abstract Envelope extent();
	
	protected abstract int count(Envelope envelope);
	
	protected abstract Collection<?> list(Envelope envelope);
	
	public void run()
	{
		Envelope envelope = extent();
		root = new Quadrant(envelope, true, true);
		root.buildTree(this);
	}
	
	public Collection<Quadrant> listResults() {
		ArrayList<Quadrant> collect = new ArrayList<>();
		root.collectInnerQuadrants(collect);
		return collect;
	}
	
	public int getOverall()
	{
		if (root != null) {
			return root.count;
		}
		else
		{
			return -1;
		}
	}


}
