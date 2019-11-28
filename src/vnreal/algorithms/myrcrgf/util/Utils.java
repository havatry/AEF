package vnreal.algorithms.myrcrgf.util;

import vnreal.constraints.demands.BandwidthDemand;
import vnreal.constraints.demands.CpuDemand;
import vnreal.constraints.resources.BandwidthResource;
import vnreal.constraints.resources.CpuResource;
import vnreal.network.Link;
import vnreal.network.Network;
import vnreal.network.Node;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class Utils {
	private static final double esp = 1e-5;
	
	public static double getBandwith(Link<?> l) {
		if (l instanceof VirtualLink) {
			return ((BandwidthDemand)l.get().get(0)).getDemandedBandwidth();
		} else {
			return ((BandwidthResource)l.get().get(0)).getAvailableBandwidth();
		}
	}
	
	public static double getCpu(Node<?> n) {
		if (n instanceof SubstrateNode) {
			return ((CpuResource)n.get().get(0)).getAvailableCycles();
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
	public static Node opposite(Link l, Node c, Network network) {
		Node s = (Node) network.getEndpoints(l).getFirst();
		Node t = (Node) network.getEndpoints(l).getSecond();
		if (c != s && c != t) {
			throw new AssertionError("The link not contains the two nodes");
		}
		return s == c ? t : s;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void ensureConnect(Network network) {
		// Ϊ����һ���ڵ㣬��ӵ��������нڵ�ı�
		Node node = (Node) network.getVertices().iterator().next();
		for (Object n : network.getVertices()) {
			if (n == node) {
				continue;
			}
			if (network.findEdge(n, node) == null) {
				// �ӱ�
				network.addEdge(network.getEdgeFactory().create(), n, node);
			}
		}
	}
	
	// ������������cdf��F(x), u����01���ȷֲ�, ��ôF-1(u)����F�ֲ�, F-1ΪF���溯��
	// ָ���ֲ���ָʾÿ�����������ͣ��ʱ��
	public static int exponentialDistribution(double lambda) {
		int x = 0;
		double y = Math.random();
		double cdf = 0.0;
		while (Utils.smallEqual(cdf, y)) {
			x++;
			cdf = 1 - Math.exp(-lambda * x);
		}
		return x;
	}
}
