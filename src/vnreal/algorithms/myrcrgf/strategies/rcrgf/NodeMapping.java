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
 * 完成节点映射
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
			vn.setReferencedValue(computeReferencedValue(vn));
			priorityQueueVirtual.offer(vn);
		}
		for (SubstrateNode sn : sNet.getVertices()) {
			sn.setReferencedValue(computeReferencedValue(sn));
			priorityQueueSubstrate.offer(sn);
		}
		
		// 依次查找
		double distanceConstraint = Double.parseDouble(Constants.PROPERTIES.getProperty("distanceConstraint"));
		MappingRule mappingRule = new MappingRule(sNet, vNet);
		
		while (!priorityQueueVirtual.isEmpty()) {
			// 逐个匹配
			VirtualNode currentVirtualNode = priorityQueueVirtual.poll();
			VirtualNode nextVirtualNode = priorityQueueVirtual.peek();
			Set<SubstrateNode> distanceDiscard = new HashSet<>(); // 由于距离因素筛选出的节点
			
			while (!priorityQueueSubstrate.isEmpty()) {
				// 到底层去找
				SubstrateNode currentSubstrateNode = priorityQueueSubstrate.poll();
				SubstrateNode nextSubstrateNode = priorityQueueSubstrate.peek();
				if (Utils.small(nextSubstrateNode.getReferencedValue(), nextVirtualNode.getReferencedValue())) {
					// 如果下一个底层资源 不能满足下一个的需求了, 保留当前的, 从由于距离因素筛选的候选集中选择一个
					if (distanceDiscard.isEmpty()) {
						// 如果候选集中没有元素
						return false;
					}
					// THIS_TODO 发送资源请求
					SubstrateNode selected = distanceDiscard.iterator().next();
					nodeMapping.put(currentVirtualNode, selected);
					// 删除该节点
					priorityQueueSubstrate.remove(selected);
					distanceDiscard.clear();
					continue; // 下一个计算
				}
				if (mappingRule.rule(currentSubstrateNode, currentVirtualNode)) {
					// 可以映射上
					if (Utils.smallEqual(computeDistance(currentSubstrateNode, currentVirtualNode), distanceConstraint)) {
						// 如果两个匹配点小于距离约束, 映射成功
						nodeMapping.put(currentVirtualNode, currentSubstrateNode);
						// THIS_TODO 发送请求资源
						continue; // 下一个计算
					} else {
						// 由于距离不满足
						distanceDiscard.add(currentSubstrateNode);
					}
				}
				priorityQueueSubstrate.offer(currentSubstrateNode); // 再加入进去
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
		return nodeMapping; // 获取节点映射
	}
}
