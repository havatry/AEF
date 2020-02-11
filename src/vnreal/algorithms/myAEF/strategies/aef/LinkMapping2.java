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

public class LinkMapping2 extends AbstractLinkMapping{
    private final int SPL; // ���̱����·��������ľ���

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
				// ����ͼ
				srcVnode = vNet.getSource(tVLink);
				dstVnode = vNet.getDest(tVLink);
			} else {
				// ����ͼ
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
					// ����������
					processedLinks = vNet.getEdges().size();
					return false;
				} else {
				    if (path_load != null && NodeLinkAssignation.verifyPath(tVLink, path_load, sNode, sNet)
                            && path_load.size() - path.size() <= SPL) {
                            path = path_load;
                    }
					// ��������
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
	 * ʹ��BFS�㷨����
	 * @return ��ȡ�����·��
	 */
	private LinkedList<SubstrateLink> getShortestPath(SubstrateNetwork substrateNetwork, 
				SubstrateNode source, SubstrateNode target, double demand) {
		Map<SubstrateNode, SubstrateNode> pre = new HashMap<SubstrateNode, SubstrateNode>(); // ��¼��ǰ�ײ�ڵ��ǰ��
		Set<SubstrateNode> visited = new HashSet<SubstrateNode>(); // ��¼�Ѿ����ʵĽڵ�
		// ����㿪ʼbfs
		Queue<SubstrateNode> queue = new LinkedList<SubstrateNode>();
		queue.offer(source);
		visited.add(source);
		while (!queue.isEmpty()) {
			SubstrateNode current = queue.poll();
			// ����������·
			for (SubstrateLink vl : substrateNetwork.getOutEdges(current)) {
				if (Utils.small(Utils.getBandwith(vl), demand)) {
					// ��ǰ��·С������
					continue;
				}
				// ��ȡ��һ���˵�
				SubstrateNode op = (SubstrateNode) Utils.opposite(vl, current, substrateNetwork);
				if (visited.contains(op)) {
					// �ڵ��Ѿ����ʹ���
					continue;
				}
				// ��������и���
				visited.add(op);
				pre.put(op, current);
				queue.add(op);
				if (op == target) {
					// find it
					LinkedList<SubstrateLink> path = new LinkedList<SubstrateLink>();
					// �������һ��·��
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
		return null; // ����û�пɴ��յ��·�� ��Ϊdemand����
	}

    /**
     * ʹ����·ʣ�����ķ���������ֵ��Ϊ���ۣ� ������·��
     * @param substrateNetwork �ײ�����
     * @param source ���
     * @param target �յ�
     * @return ��̵�·��
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
