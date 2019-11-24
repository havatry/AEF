package vnreal.algorithms.myrcrgf.strategies.rcrgf;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import vnreal.algorithms.AbstractNodeMapping;
import vnreal.algorithms.myrcrgf.util.Constants;
import vnreal.algorithms.myrcrgf.util.Utils;
import vnreal.network.Node;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

/**
 * ��ɽڵ�ӳ��
 * 2019��11��23�� ����9:55:02
 */
public class NodeMapping extends AbstractNodeMapping{
	private double distanceConstraint;
	
	public NodeMapping(double distanceConstraint, boolean nodesOverload) {
		//Created constructor stubs
		super(nodesOverload);
		this.distanceConstraint = distanceConstraint;
	}
	
	protected boolean nodeMapping(SubstrateNetwork sNet, VirtualNetwork vNet) {
		PriorityQueue<VirtualNode> priorityQueueVirtual = new PriorityQueue<>(new Comparator<VirtualNode>() {

			@Override
			public int compare(VirtualNode o1, VirtualNode o2) {
				//Created method stubs
				if (Utils.great(o1.getReferencedValue(), o2.getReferencedValue())) {
					return -1; // ����
				} else if (Utils.small(o1.getReferencedValue(), o2.getReferencedValue())) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		PriorityQueue<SubstrateNode> priorityQueueSubstrate = new PriorityQueue<>(new Comparator<SubstrateNode>() {

			@Override
			public int compare(SubstrateNode o1, SubstrateNode o2) {
				//Created method stubs
				if (Utils.great(o1.getReferencedValue(), o2.getReferencedValue())) {
					return -1;
				} else if (Utils.small(o1.getReferencedValue(), o2.getReferencedValue())) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		
		// 1.�������������referenced value, ��Щ�����ڽڵ���
		for (VirtualNode vn : vNet.getVertices()) {
			vn.setReferencedValue(computeReferencedValue(vn));
			priorityQueueVirtual.offer(vn);
		}
		for (SubstrateNode sn : sNet.getVertices()) {
			sn.setReferencedValue(computeReferencedValue(sn));
			priorityQueueSubstrate.offer(sn);
		}
		
		// ���β���
		double distanceConstraint = Double.parseDouble(Constants.PROPERTIES.getProperty("distanceConstraint"));
		MappingRule mappingRule = new MappingRule(sNet, vNet);
		
		while (!priorityQueueVirtual.isEmpty()) {
			// ���ƥ��
			VirtualNode currentVirtualNode = priorityQueueVirtual.poll();
			VirtualNode nextVirtualNode = priorityQueueVirtual.peek();
			Set<SubstrateNode> distanceDiscard = new HashSet<>(); // ���ھ�������ɸѡ���Ľڵ�
			
			while (!priorityQueueSubstrate.isEmpty()) {
				// ���ײ�ȥ��
				SubstrateNode currentSubstrateNode = priorityQueueSubstrate.poll();
				SubstrateNode nextSubstrateNode = priorityQueueSubstrate.peek();
				if (Utils.small(nextSubstrateNode.getReferencedValue(), nextVirtualNode.getReferencedValue())) {
					// �����һ���ײ���Դ ����������һ����������, ������ǰ��, �����ھ�������ɸѡ�ĺ�ѡ����ѡ��һ��
					if (distanceDiscard.isEmpty()) {
						// �����ѡ����û��Ԫ��
						return false;
					}
					// THIS_TODO ������Դ����
					SubstrateNode selected = distanceDiscard.iterator().next();
					nodeMapping.put(currentVirtualNode, selected);
					// ɾ���ýڵ�
					priorityQueueSubstrate.remove(selected);
					distanceDiscard.clear();
					continue; // ��һ������
				}
				if (mappingRule.rule(currentSubstrateNode, currentVirtualNode)) {
					// ����ӳ����
					if (Utils.smallEqual(computeDistance(currentSubstrateNode, currentVirtualNode), distanceConstraint)) {
						// �������ƥ���С�ھ���Լ��, ӳ��ɹ�
						nodeMapping.put(currentVirtualNode, currentSubstrateNode);
						// THIS_TODO ����������Դ
						continue; // ��һ������
					} else {
						// ���ھ��벻����
						distanceDiscard.add(currentSubstrateNode);
					}
				}
				priorityQueueSubstrate.offer(currentSubstrateNode); // �ټ����ȥ
			}
		}
		return true;
	}
	
	private double computeReferencedValue(Node<?> node) {
		return 0.0;
	}
	
	private double computeDistance(Node<?> o1, Node<?> o2) {
		return Math.sqrt(Math.pow(o1.getCoordinateX() - o2.getCoordinateX(), 2) 
				+ Math.pow(o1.getCoordinateY() - o2.getCoordinateY(), 2));
	}
	
	public Map<VirtualNode, SubstrateNode> getNodeMapping() {
		return nodeMapping; // ��ȡ�ڵ�ӳ��
	}
}
