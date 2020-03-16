package vnreal.algorithms.myAEF.strategies.aef;

import edu.uci.ics.jung.graph.util.Pair;
import mulavito.algorithms.shortestpath.ksp.Yen;
import org.apache.commons.collections15.Transformer;
import vnreal.algorithms.AbstractLinkMapping;
import vnreal.algorithms.myAEF.util.Constants;
import vnreal.algorithms.myAEF.util.Utils;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.generators.topologies.transitstub.graph.Graph;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

import java.util.*;

/**
 * 这是增加了链路负载均衡策略相比于该包下的LinkMapping类
 */
public class LinkMapping2 extends AbstractLinkMapping{
    private final int SPL; // 容忍比最短路径所多出的距离

    public LinkMapping2(int SPL) {
        this.SPL = SPL;
    }
	
	@Override
	protected boolean linkMapping(SubstrateNetwork sNet, VirtualNetwork vNet,
			Map<VirtualNode, SubstrateNode> nodeMapping) {
		// TODO Auto-generated method stub
		this.processedLinks = 0;
		this.mappedLinks = 0;
		for (VirtualLink tVLink : vNet.getEdges()) {
			processedLinks++;
			VirtualNode srcVnode = null;
			VirtualNode dstVnode = null;
			if (Constants.DIRECTED) {
				// 有向图
				srcVnode = vNet.getSource(tVLink);
				dstVnode = vNet.getDest(tVLink);
			} else {
				// 无向图
				Pair<VirtualNode> p = vNet.getEndpoints(tVLink);
				srcVnode = p.getFirst();
				dstVnode = p.getSecond();
			}
			final SubstrateNode sNode = nodeMapping.get(srcVnode);
			final SubstrateNode dNode = nodeMapping.get(dstVnode);
			List<SubstrateLink> path = getShortestPath(sNet, sNode, dNode, Utils.getBandwith(tVLink));
			List<SubstrateLink> path_load = getShortestPath2(sNet, sNode, dNode, Utils.getBandwith(tVLink));
			if (!sNode.equals(dNode)) {
				if (path == null || !NodeLinkAssignation.verifyPath(tVLink, path, sNode, sNet)) {
					// 不满足需求
					processedLinks = vNet.getEdges().size();
					return false;
				} else {
				    if (path_load != null && NodeLinkAssignation.verifyPath(tVLink, path_load, sNode, sNet)
                            && path_load.size() - path.size() <= SPL) {
                            path = path_load;
                    }
					// 满足需求
					if (!NodeLinkAssignation.vlm(tVLink, path, sNet, sNode)) {
						throw new AssertionError("But we checked before!");
					}
					linkMapping.put(tVLink, path);
					mappedLinks++;
				}
			}
		}
		return true;
	}
	
	/**
	 * 使用BFS算法来找
	 * @return 获取的最短路径
	 */
	private LinkedList<SubstrateLink> getShortestPath(SubstrateNetwork substrateNetwork, 
				SubstrateNode source, SubstrateNode target, double demand) {
		Map<SubstrateNode, SubstrateNode> pre = new HashMap<SubstrateNode, SubstrateNode>(); // 记录当前底层节点的前驱
		Set<SubstrateNode> visited = new HashSet<SubstrateNode>(); // 记录已经访问的节点
		// 从起点开始bfs
		Queue<SubstrateNode> queue = new LinkedList<SubstrateNode>();
		queue.offer(source);
		visited.add(source);
		while (!queue.isEmpty()) {
			SubstrateNode current = queue.poll();
			// 遍历所有链路
			for (SubstrateLink vl : substrateNetwork.getOutEdges(current)) {
				if (Utils.small(Utils.getBandwith(vl), demand)) {
					// 当前链路小于需求
					continue;
				}
				// 获取另一个端点
				SubstrateNode op = (SubstrateNode) Utils.opposite(vl, current, substrateNetwork);
				if (visited.contains(op)) {
					// 节点已经访问过了
					continue;
				}
				// 否则则进行更新
				visited.add(op);
				pre.put(op, current);
				queue.add(op);
				if (op == target) {
					// find it
					LinkedList<SubstrateLink> path = new LinkedList<SubstrateLink>();
					// 逆序输出一个路径
					while (pre.get(op) != null) {
						SubstrateLink link = substrateNetwork.findEdge(op, pre.get(op));
						path.offerFirst(link);
						op = pre.get(op);
					}
					if (path.size() == 0) {
						// not found
						return null;
					}
					return path;
				}
			}
		}
//		throw new AssertionError("not can be arrived here");
		return null; // 由于没有可达终点的路径 因为demand限制
	}

    /**
     * 使用链路剩余带宽的反比例函数值作为代价， 求出最短路径
     * @param substrateNetwork 底层网络
     * @param source 起点
     * @param target 终点
     * @return 最短的路径
     */
	private List<SubstrateLink> getShortestPath2(SubstrateNetwork substrateNetwork, SubstrateNode source,
                                                       SubstrateNode target, double demand) {
        Transformer<SubstrateLink, Double> nev = sl -> {
            double extra_bandwidth = Utils.getBandwith(sl);
            if (Utils.small(extra_bandwidth, demand)) {
                return Constants.BIG_NUM;
            } else {
                return Constants.C / extra_bandwidth;
            }
        };
        Yen<SubstrateNode, SubstrateLink> yen = new Yen(substrateNetwork, nev);
        List<List<SubstrateLink>> path = yen.getShortestPaths(source, target, 1);
        if (path == null || path.size() == 0) {
            return null;
        }
        return path.get(0);
    }
}
