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
package vnreal.network.substrate;

import vnreal.constraints.resources.AbstractResource;
import vnreal.network.Node;

/**
 * A substrate network node class.
 * 
 * @author Michael Duelli
 * @author Vlad Singeorzan
 */
public class SubstrateNode extends Node<AbstractResource> {
	private double referencedValue;
	
	public SubstrateNode() {
		super(0);
	}

	public SubstrateNode getCopy(boolean deepCopy) {
		SubstrateNode clone = new SubstrateNode();
		clone.setName(getName());

		for (AbstractResource r : this) {
			if (deepCopy)
				clone.add(r.getCopy(clone, true));
			else
				clone.add(r);
		}

		return clone;
	}

	@Override
	public String toString() {
		return "SubstrateNode(" + getName() + ")" + " [" + get() + "]";
	}

	@Override
	public String toStringShort() {
		return "SN(" + getId() + ")";
	}
	
	public void setReferencedValue(double referencedValue) {
		this.referencedValue = referencedValue;
	}
	
	public double getReferencedValue() {
		return referencedValue;
	}
}
