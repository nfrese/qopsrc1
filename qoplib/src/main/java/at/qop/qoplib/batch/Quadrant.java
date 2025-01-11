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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.locationtech.jts.geom.Envelope;

public class Quadrant {

	public final Envelope envelope;
	private final boolean atRightBorder;
	private final boolean atBottomBorder;
	
	public Quadrant[] children = null;

	public int count;
	
	private CountRecs countRecs = null;
	
	public Quadrant(Envelope envelope, boolean atRightBorder, boolean atBottomBorder) {
		super();
		this.envelope = envelope;
		this.atRightBorder = atRightBorder;
		this.atBottomBorder = atBottomBorder;
	}

	public void buildTree(Quadify ref)
	{
		count = count(ref, envelope, atRightBorder, atBottomBorder);
		if (count > ref.maxPerRect)
		{
			int childrenCount=0;
			// we have to break it up			
			children = new Quadrant[4];
			Envelope[] envelopes = breakUpEnvelope(envelope);
			for (int i=0; i<4; i++)
			{
				boolean childAtRightBorder = atRightBorder && (i == 0 || i == 3);
				boolean childAtBottomBorder = atBottomBorder && (i == 2 || i == 3);
				
				Quadrant child = new Quadrant(envelopes[i], childAtRightBorder, childAtBottomBorder);
				child.buildTree(ref);
				childrenCount += child.count;
				children[i] = child;
			}
			
			if (count != childrenCount) // check plausibility
			{
				throw new RuntimeException("count != childrenCount");
			}
		}
	}

	public void collectInnerQuadrants(Collection<Quadrant> quadrants)
	{
		if (children != null)
		{
			for (Quadrant child : children)
			{
				child.collectInnerQuadrants(quadrants);
			}
		}
		else
		{
			quadrants.add(this);
		}
	}
	
	
	public static Envelope[] breakUpEnvelope(Envelope envelope)
	{
		Envelope[] quadrants = new Envelope[4];
		{
			//OX first quadrant!
			//OO
			
			double minX = envelope.getMinX() + envelope.getWidth()/2;
			double maxX = envelope.getMaxX();
			double minY = envelope.getMinY()+ envelope.getHeight()/2;
			double maxY = envelope.getMaxY();
			
			quadrants[0] = new Envelope(minX, maxX, minY, maxY);
					
		}
		{
			//XO second quadrant!
			//OO
			
			double minX = envelope.getMinX() ;
			double maxX = envelope.getMaxX() - envelope.getWidth()/2;
			double minY = envelope.getMinY() + envelope.getHeight()/2;
			double maxY = envelope.getMaxY();
			
			quadrants[1] = new Envelope(minX, maxX, minY, maxY);
					
		}
		{
			//OO third quadrant!
			//XO
			
			double minX = envelope.getMinX();
			double maxX = envelope.getMaxX()- envelope.getWidth()/2;
			double minY = envelope.getMinY();
			double maxY = envelope.getMaxY()- envelope.getHeight()/2;
			
			quadrants[2] = new Envelope(minX, maxX, minY, maxY);
					
		}
		{
			//OO fourth quadrant!
			//OX
			
			double minX = envelope.getMinX() + envelope.getWidth()/2;
			double maxX = envelope.getMaxX();
			double minY = envelope.getMinY();
			double maxY = envelope.getMaxY()- envelope.getHeight()/2;
			
			quadrants[3] = new Envelope(minX, maxX, minY, maxY);
		}
		return quadrants;
	}

	
	private int count(Quadify ref, Envelope envelope, boolean atRightBorder, boolean atBottomBorder) {
		return countSimple(ref, envelope, atRightBorder, atBottomBorder);
	}	
	
	public int countSimple(Quadify ref, Envelope envelope, boolean atRightBorder, boolean atBottomBorder) {
		
		final int allCnt = ref.count(envelope);
		int result = allCnt;
		int cntAtRightBorder = -1;
		int cntAtBottomBorder = -1;
		int rightBottomCornerCount = -1;
		
		if (!atRightBorder)
		{
			Envelope rightBorderEnvelope = rightBorderEnvelope(envelope);
			cntAtRightBorder = ref.count(rightBorderEnvelope);
			result -= cntAtRightBorder;
		}

		if (!atBottomBorder)
		{
			Envelope bottomBorderEnvelope = bottomBorderEnvelope(envelope);
			cntAtBottomBorder = ref.count(bottomBorderEnvelope);
			
			if (!atRightBorder)
			{
				Envelope rightBottomEnvelope = bottomRightCornerEnvelope(envelope);
				rightBottomCornerCount = ref.count(rightBottomEnvelope);
				cntAtBottomBorder -= rightBottomCornerCount;
			}
			result -= cntAtBottomBorder;
		}

		if (result < 0)
		{
			throw new RuntimeException("unexpected result<0! was "+ result);
		}
		
		return result;
	}
	
	private static class CountRecs {
		Collection<?> allCnt;
		Collection<?> cntAtRightBorder;
		Collection<?> cntAtBottomBorder;
		Collection<?> rightBottomCornerCount;
		Set<Object> result;
	}
	
	public int countRecs(Quadify ref, Envelope envelope, boolean atRightBorder, boolean atBottomBorder) {
		countRecs = new CountRecs();
		
		countRecs.allCnt = ref.list(envelope);
		countRecs.result = new HashSet<Object>();
		countRecs.result.addAll(countRecs.allCnt);
		
		countRecs.cntAtRightBorder = null;
		countRecs.cntAtBottomBorder = null;
		countRecs.rightBottomCornerCount = null;
		
		if (!atRightBorder)
		{
			Envelope rightBorderEnvelope = rightBorderEnvelope(envelope);
			countRecs.cntAtRightBorder = ref.list(rightBorderEnvelope);
			countRecs.result.removeAll(countRecs.cntAtRightBorder);
		}

		if (!atBottomBorder)
		{
			Envelope bottomBorderEnvelope = bottomBorderEnvelope(envelope);
			countRecs.cntAtBottomBorder = ref.list(bottomBorderEnvelope);
			
			if (!atRightBorder)
			{
				Envelope rightBottomEnvelope = bottomRightCornerEnvelope(envelope);
				countRecs.rightBottomCornerCount = ref.list(rightBottomEnvelope);
				countRecs.cntAtBottomBorder.removeAll(countRecs.rightBottomCornerCount);
			}
			countRecs.result.removeAll(countRecs.cntAtBottomBorder);
		}

		return countRecs.result.size();
	}

	public static Envelope bottomRightCornerEnvelope(Envelope envelope) {
		return new Envelope(envelope.getMaxX(), envelope.getMaxX(), envelope.getMinY(), envelope.getMinY());	
	}

	public static Envelope bottomBorderEnvelope(Envelope envelope) {
		return new Envelope(envelope.getMinX(), envelope.getMaxX(), envelope.getMinY(), envelope.getMinY());
	}

	public static Envelope rightBorderEnvelope(Envelope envelope) {
		return new Envelope(envelope.getMaxX(), envelope.getMaxX(), envelope.getMinY(), envelope.getMaxY());
	}

	@Override
	public String toString() {
		return "{count: " + count + ", "
				+ (countRecs != null ? "countRecs: " + countRecs.result + ", " : "")
				+ "envelope: '" + envelope + "', "
				+ "atRightBorder: " + atRightBorder + ", "
				+ "atBottomBorder: " + atBottomBorder + ", "
				+ "children:[" + Arrays.toString(children) + "]}";
	}
	
}
