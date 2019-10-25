/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2010-2011, The VNREAL Project Team.
 * 
 * This work has been funded by the European FP7
 * Network of Excellence "Euro-NF" (grant agreement no. 216366)
 * through the Specific Joint Developments and Experiments Project
 * "Virtual Network Resource Embedding Algorithms" (VNREAL). 
 *
 * The VNREAL Project Team consists of members from:
 * - University of Wuerzburg, Germany
 * - Universitat Politecnica de Catalunya, Spain
 * - University of Passau, Germany
 * See the file AUTHORS for details and contact information.
 * 
 * This file is part of ALEVIN (ALgorithms for Embedding VIrtual Networks).
 *
 * ALEVIN is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License Version 3 or later
 * (the "GPL"), or the GNU Lesser General Public License Version 3 or later
 * (the "LGPL") as published by the Free Software Foundation.
 *
 * ALEVIN is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * or the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License and
 * GNU Lesser General Public License along with ALEVIN; see the file
 * COPYING. If not, see <http://www.gnu.org/licenses/>.
 *
 * ***** END LICENSE BLOCK ***** */
package vnreal.network.virtual;

import vnreal.algorithms.argf.util.DTOVirtual;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.network.Node;

/**
 * A virtual network node class.
 * 
 * @author Michael Duelli
 * @author Vlad Singeorzan
 */
public class VirtualNode extends Node<AbstractDemand> {
	private DTOVirtual dtoVirtual = new DTOVirtual();
	
	public VirtualNode(int layer) {
		super(layer);
	}

	@Override
	public String toString() {
		return "VirtualNode(" + getName() + " " + getId() + ")@" + getLayer() + " [" + get() + "]";
	}

	@Override
	public String toStringShort() {
		return "VN(" + getName() + " " + getId() + ")";
	}
	
	public VirtualNode getCopy(boolean deepCopy) {
		return getCopy(deepCopy, new VirtualNode(getLayer()));
	}
	
	public VirtualNode getCopy(boolean deepCopy, VirtualNode result) {
		result.setName(getName());
		for (AbstractDemand r : this) {
			if (deepCopy)
				result.add(r.getCopy(result));
			else
				result.add(r);
		}
		return result;
	}

	public DTOVirtual getDtoVirtual() {
		return dtoVirtual;
	}
}
