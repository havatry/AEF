package vnreal.algorithms.myAEF.strategies.rcrgf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import edu.uci.ics.jung.graph.util.Pair;
import vnreal.algorithms.AbstractLinkMapping;
import vnreal.algorithms.myAEF.util.Constants;
import vnreal.algorithms.myAEF.util.Utils;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class LinkMapping extends AbstractLinkMapping{
	
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
			if (!sNode.equals(dNode)) {
				if (path == null || !NodeLinkAssignation.verifyPath(tVLink, path, sNode, sNet)) {
					// ����������
					processedLinks = vNet.getEdges().size();
					return false;
				} else {
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
	 * @param substrateNetwork �ײ�����
	 * @param virtualLink ������·
	 * @param nodeMapping ӳ����
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
}
