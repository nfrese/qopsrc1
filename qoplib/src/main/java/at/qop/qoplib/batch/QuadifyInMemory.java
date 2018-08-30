package at.qop.qoplib.batch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.strtree.STRtree;

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