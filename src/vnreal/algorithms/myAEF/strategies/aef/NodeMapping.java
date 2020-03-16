package vnreal.algorithms.myAEF.strategies.aef;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;

import vnreal.algorithms.AbstractNodeMapping;
import vnreal.algorithms.myAEF.util.Utils;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

/**
 * 完成节点映射
 * 与save包中的NodeMapping2相比，这里的软距离计算是平均距离，而后者是直接欧式距离
 * 2019年11月23日 下午9:55:02
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
					return -1; // 逆序
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
		
		// 1.计算虚拟网络的referenced value, 这些包含在节点中
		for (VirtualNode vn : vNet.getVertices()) {
			vn.setReferencedValue(computeReferencedValueForVirtual(vNet, vn));
			priorityQueueVirtual.offer(vn);
		}
		for (SubstrateNode sn : sNet.getVertices()) {
			sn.setReferencedValue(computeReferencedValueForSubstrate(sNet, sn));
			priorityQueueSubstrate.offer(sn);
		}
		// 增加一个额外节点
		SubstrateNode spec = new SubstrateNode();
		spec.setReferencedValue(-1);
		priorityQueueSubstrate.offer(spec);
		// 依次查找
		MappingRule mappingRule = new MappingRule(sNet, vNet);
		Set<SubstrateNode> distanceDiscard = new HashSet<>(); // 由于距离因素筛选出的节点
		Set<SubstrateNode> ruleDiscard = new HashSet<SubstrateNode>(); // 由于规则因素筛选出的节点
		while (!priorityQueueVirtual.isEmpty()) {
			// 逐个匹配
			VirtualNode currentVirtualNode = priorityQueueVirtual.poll();
			distanceDiscard.clear();
			ruleDiscard.clear();
			
			while (!priorityQueueSubstrate.isEmpty()) {
				// 到底层去找
				SubstrateNode currentSubstrateNode = priorityQueueSubstrate.poll();
				if (priorityQueueSubstrate.isEmpty() ||
						Utils.small(currentSubstrateNode.getReferencedValue(), currentVirtualNode.getReferencedValue())) {
					// 如果底层资源 不能满足需求了, 保留当前的, 从由于距离因素筛选的候选集中选择一个
					if (distanceDiscard.isEmpty()) {
						// 如果候选集中没有元素
						return false;
					}
					Iterator<SubstrateNode> iter = distanceDiscard.iterator();
					SubstrateNode selected =iter.next(); // 选择第一个 TODO 可以考虑选择资源更多的其中一个节点
					NodeLinkAssignation.vnm(currentVirtualNode, selected);
					nodeMapping.put(currentVirtualNode, selected);
					// 删除该节点
					iter.remove();
					break; // 下一个计算
				}
				if (mappingRule.rule(currentSubstrateNode, currentVirtualNode)) {
					// 可以映射上
					if (Utils.smallEqual(computeDistance(sNet, vNet, currentSubstrateNode, currentVirtualNode),
							distanceConstraint)) {
						NodeLinkAssignation.vnm(currentVirtualNode, currentSubstrateNode); // 映射
						nodeMapping.put(currentVirtualNode, currentSubstrateNode);
						break; // 下一个计算
					} else {
						// 由于距离不满足
						distanceDiscard.add(currentSubstrateNode);
					}
				} else {
					// 因为规则不满足
					ruleDiscard.add(currentSubstrateNode);
				}
			}
			priorityQueueSubstrate.addAll(distanceDiscard);
			priorityQueueSubstrate.addAll(ruleDiscard);
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
	
	private double computeDistance(SubstrateNetwork sNet, VirtualNetwork vNet, SubstrateNode o1, VirtualNode o2) {
		// 获取所有已经映射的虚拟邻居节点
		Collection<VirtualNode> neighbors = vNet.getNeighbors(o2);
		Collection<SubstrateNode> candicates = new LinkedList<SubstrateNode>();
		for (VirtualNode vn : neighbors) {
			if (nodeMapping.get(vn) != null) {
				// 已经映射的底层节点
				candicates.add(nodeMapping.get(vn));
			}
		}
		if (candicates.isEmpty()) {
			// 没有映射到底层节点 那么距离是最小的
			return 0.0;
		}
		double tdistance = 0.0;
		for (SubstrateNode sn : candicates) {
			tdistance += Math.sqrt(Math.pow(o1.getCoordinateX() - sn.getCoordinateX(), 2) 
					+ Math.pow(o1.getCoordinateY() - sn.getCoordinateY(), 2));
		}
		return tdistance / candicates.size();
	}
}
