package vnreal.algorithms.myrcrgf.strategies.rcrgf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import vnreal.algorithms.AbstractNodeMapping;
import vnreal.algorithms.myrcrgf.util.Utils;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.network.Node;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
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
			vn.setReferencedValue(computeReferencedValueForVirtual(vNet, vn));
			priorityQueueVirtual.offer(vn);
		}
		for (SubstrateNode sn : sNet.getVertices()) {
			sn.setReferencedValue(computeReferencedValueForSubstrate(sNet, sn));
			priorityQueueSubstrate.offer(sn);
		}
		// ����һ������ڵ�
		SubstrateNode spec = new SubstrateNode();
		spec.setReferencedValue(-1);
		priorityQueueSubstrate.offer(spec);
		
		// ���β���
		MappingRule mappingRule = new MappingRule(sNet, vNet);
		
		while (!priorityQueueVirtual.isEmpty()) {
			// ���ƥ��
			VirtualNode currentVirtualNode = priorityQueueVirtual.poll();
			Set<SubstrateNode> distanceDiscard = new HashSet<>(); // ���ھ�������ɸѡ���Ľڵ�
			
			while (!priorityQueueSubstrate.isEmpty()) {
				// ���ײ�ȥ��
				SubstrateNode currentSubstrateNode = priorityQueueSubstrate.poll();
				if (priorityQueueSubstrate.isEmpty() ||
						Utils.small(currentSubstrateNode.getReferencedValue(), currentVirtualNode.getReferencedValue())) {
					// ����ײ���Դ ��������������, ������ǰ��, �����ھ�������ɸѡ�ĺ�ѡ����ѡ��һ��
					if (distanceDiscard.isEmpty()) {
						// �����ѡ����û��Ԫ��
						return false;
					}
					Iterator<SubstrateNode> iter = distanceDiscard.iterator();
					SubstrateNode selected =iter.next(); // ѡ���һ�� TODO ���Կ���ѡ����Դ���������һ���ڵ�
					NodeLinkAssignation.vnm(currentVirtualNode, selected);
					nodeMapping.put(currentVirtualNode, selected);
					// ɾ���ýڵ�
					iter.remove();
					break; // ��һ������
				}
				if (mappingRule.rule(currentSubstrateNode, currentVirtualNode)) {
					// ����ӳ����
					if (Utils.smallEqual(computeDistance(currentSubstrateNode, currentVirtualNode), distanceConstraint)) {
						NodeLinkAssignation.vnm(currentVirtualNode, currentSubstrateNode); // ӳ��
						nodeMapping.put(currentVirtualNode, currentSubstrateNode);
						break; // ��һ������
					} else {
						// ���ھ��벻����
						distanceDiscard.add(currentSubstrateNode);
					}
				}
			}
			priorityQueueSubstrate.addAll(distanceDiscard);
		}
		return true;
	}
	
	private double computeReferencedValueForVirtual(VirtualNetwork vNet, VirtualNode vn) {
		double result = 0.0;
		double cpuDemand = Utils.getCpu(vn);
		
		for (VirtualLink vl : vNet.getOutEdges(vn)) {
			result += cpuDemand * Utils.getBandwith(vl);
		}
		
		return result;
	}
	
	private double computeReferencedValueForSubstrate(SubstrateNetwork sNet, SubstrateNode sn) {
		double result = 0.0;
		double cpuResource = Utils.getCpu(sn);
		
		for (SubstrateLink sl : sNet.getOutEdges(sn)) {
			result += cpuResource * Utils.getBandwith(sl);
		}
		
		return result;
	}
	
	private double computeDistance(Node<?> o1, Node<?> o2) {
		return Math.sqrt(Math.pow(o1.getCoordinateX() - o2.getCoordinateX(), 2) 
				+ Math.pow(o1.getCoordinateY() - o2.getCoordinateY(), 2));
	}
}
