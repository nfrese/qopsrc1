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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.strtree.STRtree;

import at.qop.qoplib.Utils;

public class QuadifyInMemory<O> extends Quadify
{
	STRtree tree = new STRtree();
	Envelope envelope = null;
	
	public QuadifyInMemory(int maxPerRect) {
		super(maxPerRect);
	}

	@Override
	protected Envelope extent() {
		return envelope;
	}
	
	@Override
	protected int count(Envelope searchEnv) {
		return list(searchEnv).size();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Collection<O> list(Envelope searchEnv) {
		final ArrayList<O> items = new ArrayList<>();
		
		tree.query(searchEnv, item -> {
			items.add((O) item);
		});
		return items;
	}
	
	public void add(Geometry geom, O item)
	{
		Envelope itemEnvelope = geom.getEnvelopeInternal();
		tree.insert(itemEnvelope, item);
		
		if (envelope == null)
		{
			try {
				envelope = Utils.deepClone(itemEnvelope);
			} catch (ClassNotFoundException | IOException e) {
				throw new RuntimeException(e);
			}
		}
		else
		{
			envelope.expandToInclude(itemEnvelope);
		}
	}
}