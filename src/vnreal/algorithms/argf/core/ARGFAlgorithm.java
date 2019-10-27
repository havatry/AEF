package vnreal.algorithms.argf.core;

import vnreal.algorithms.argf.util.Utils;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualNetwork;

public class ARGFAlgorithm {
	public boolean compute(SubstrateNetwork substrateNetwork, VirtualNetwork virtualNetwork) {
		Utils.initMapSubstrateNode(substrateNetwork.getVertices());
		Utils.initMapVirtualNode(virtualNetwork.getVertices());
		return new MainAlgorithm(substrateNetwork, virtualNetwork).work();
	}
}
