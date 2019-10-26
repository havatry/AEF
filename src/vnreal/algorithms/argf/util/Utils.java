package vnreal.algorithms.argf.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.BandwidthDemand;
import vnreal.constraints.demands.CpuDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.BandwidthResource;
import vnreal.constraints.resources.CpuResource;
import vnreal.network.Link;
import vnreal.network.Network;
import vnreal.network.Node;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNode;

public class Utils {
	private static final double esp = 1e-3;
	private static final Map<Long, Integer> mapVirtualNode = new HashMap<Long, Integer>();
	private static final Map<Long, Integer> mapSubstrateNode = new HashMap<>();
	
	public static double getBandwith(Link<?> l) {
		if (l instanceof VirtualLink) {
			return ((BandwidthDemand)l.get().get(0)).getDemandedBandwidth();
		} else {
			return ((BandwidthResource)l.get().get(0)).getBandwidth() - ((BandwidthResource)l.get().get(0)).getOccupiedBandwidth();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static void setBandwith(Link l, double bandwith) {
		if (l instanceof VirtualLink) {
			((BandwidthDemand)l.get().get(0)).setDemandedBandwidth(bandwith);
		} else {
			((BandwidthResource)l.get().get(0)).setBandwidth(bandwith);
		}
	}
	
	public static double getCpu(Node<?> n) {
		if (n instanceof SubstrateNode) {
			return ((CpuResource)n.get().get(0)).getCycles() - ((CpuResource)n.get().get(0)).getOccupiedCycles();
		} else {
			return ((CpuDemand)n.get().get(0)).getDemandedCycles();
		}
	}
	
	public static boolean equal(double a, double b) {
		return a <= b + esp && a >= b - esp;
	}
	
	public static boolean great(double a, double b) {
		return a > b + esp;
	}
	
	public static boolean small(double a, double b) {
		return a < b - esp;
	}
	
	public static boolean greatEqual(double a, double b) {
		return !small(a, b);
	}
	
	public static boolean smallEqual(double a, double b) {
		return !great(a, b);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static double getReferencedResource(Node node, Network network, double alpha) {
		double bandwith_total = 0.0;
		for (Object l : network.getOutEdges(node)) {
			Link<?> link = (Link<?>)l;
			bandwith_total += alpha * getBandwith(link);
		}
		double v1 = getCpu(node) + bandwith_total;
		return v1;
	}
	
	public static void initMapVirtualNode(Collection<VirtualNode> virtualNodes) {
		int index = 0;
		for (VirtualNode vn : virtualNodes) {
			mapVirtualNode.put(vn.getId(), index++);
		}
	}
	
	public static void initMapSubstrateNode(Collection<SubstrateNode> substrateNodes) {
		int index = 0;
		for (SubstrateNode sn : substrateNodes) {
			mapSubstrateNode.put(sn.getId(), index++);
		}
	}
	
	public static int getIndexForVirtualNode(VirtualNode vn) {
		return mapVirtualNode.get(vn.getId());
	}
	
	public static int getIndexForSubstrateNode(SubstrateNode sn) {
		return mapSubstrateNode.get(sn.getId());
	}
	
	public static double computeSE(double previousSE, SubstrateLink sl) {
		double bandwith = getBandwith(sl);
		if (equal(previousSE, 0.0)) {
			return bandwith;
		} else {
			return Math.min(bandwith, previousSE);
		}
	}
	
	public static List<SubstrateNode> filter(List<SubstrateNode> list, SubstrateNode spec, double cpuConstraint, double bandwithConstraint) {
		List<SubstrateNode> result = new LinkedList<SubstrateNode>();
		for (SubstrateNode sn : list) {
			if (small(Utils.getCpu(sn), cpuConstraint)) {
				// ��ѡ�ڵ㲻����cpu����
				continue;
			}
			if (small(sn.getDtoSubstrate().getBTL()[getIndexForSubstrateNode(spec)], bandwithConstraint)) {
				// ��ѡ�ڵ㲻����bandwith����
				continue;
			}
			result.add(sn);
		}
		return result;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Node opposite(Link l, Node c, Network network) {
		Node s = (Node) network.getEndpoints(l).getFirst();
		Node t = (Node) network.getEndpoints(l).getSecond();
		if (c != s && c != t) {
			throw new AssertionError("The link not contains the two nodes");
		}
		return s == c ? t : s;
	}
	
	public static void vnm(VirtualNode vn, SubstrateNode sn) {
		for (AbstractDemand dem : vn) {
			boolean fulfilled = false;
			for (AbstractResource res : sn)
				if (res.accepts(dem) && res.fulfills(dem) && dem.occupy(res)) {
					fulfilled = true;
					break;
				}

			if (!fulfilled)
				throw new AssertionError("But we checked before!");
		}
	}

	public static void vlm(VirtualLink vl, List<SubstrateLink> spath) {
		for (SubstrateLink sl : spath)
			// ... a resource to each link demand must be assigned.
			for (AbstractDemand dem : vl) {
				boolean fulfilled = false;

				// FIXME Consider resources of intermediate nodes.
				// if (req instanceof INodeConstraint)

				for (AbstractResource res : sl)
					if (res.accepts(dem) && res.fulfills(dem)
							&& dem.occupy(res)) {
						fulfilled = true;
						break;
					}

				if (!fulfilled)
					throw new AssertionError("But we checked before!");
			}
	}
	
	public static void processBTL(SubstrateNode sn, int length) {
		if (sn.getDtoSubstrate().getBTL() == null) {
			// ��ʼ��
			sn.getDtoSubstrate().setBTL(new double[length]);
		}
	}
}