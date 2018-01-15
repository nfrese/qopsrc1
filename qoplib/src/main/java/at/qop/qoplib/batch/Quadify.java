package at.qop.qoplib.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
	
	public Collection<Quadrant> listInnerQuadrants() {
		ArrayList<Quadrant> collect = new ArrayList<>();
		root.collectInnerQuadrants(collect);
		return collect;
	}



	
}
