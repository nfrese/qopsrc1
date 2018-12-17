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

package at.qop.qoplib.calculation;

import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.dbconnector.DbRecord;

public class CreateTargetsMulti extends CreateTargets<MultiTarget> {

	protected MultiTarget createParent(DbRecord rec, Geometry shape) {
		MultiTarget parentLt = new MultiTarget();
		parentLt.geom = shape; 
		parentLt.rec = rec;
		return parentLt;
	}
	
	protected void addTarget(List<MultiTarget> results, DbRecord rec, Geometry shape) {
		MultiTarget lt = new MultiTarget();
		
		lt.geom = shape; 
		lt.rec = rec;
		results.add(lt);
	}
	
	protected void addTargetDissolved(List<MultiTarget> results, MultiTarget parentLt, DbRecord rec,
			Point point) {
		MultiTargetDissolved lt = new MultiTargetDissolved();
		lt.parent = parentLt;
		lt.geom = point; 
		lt.rec = rec;
		results.add(lt);
	}
}
