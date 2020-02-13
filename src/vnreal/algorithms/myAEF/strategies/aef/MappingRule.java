package vnreal.algorithms.myAEF.strategies.aef;

import java.util.Collections;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vnreal.algorithms.myAEF.util.Utils;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

/**
 * ӳ�����
 * 2019��11��20�� ����9:42:05
 */
public class MappingRule {
	private SubstrateNetwork substrateNetwork;
	private VirtualNetwork virtualNetwork;
	private Logger log = LoggerFactory.getLogger(MappingRule.class);
	
	public MappingRule(SubstrateNetwork substrateNetwork, VirtualNetwork virtualNetwork) {
		//Created constructor stubs
		this.substrateNetwork = substrateNetwork;
		this.virtualNetwork = virtualNetwork;
	}
	
	/**
	 * �ж�����ڵ�ӳ�䵽�ײ�ڵ��Ƿ����
	 * @param substrateNode 
	 * @param virtualNode
	 * @return
	 */
	public boolean rule(SubstrateNode substrateNode, VirtualNode virtualNode) {
		return ruleForNode(substrateNode, virtualNode) && ruleForLink(substrateNode, virtualNode);
	}
	
	private boolean ruleForNode(SubstrateNode substrateNode, VirtualNode virtualNode) {
		// ֻ��Ҫ�жϽڵ��Ƿ��������󼴿� �ײ���Դ >= ��������
		return Utils.greatEqual(Utils.getCpu(substrateNode), Utils.getCpu(virtualNode));
	}
	
	private boolean ruleForLink(SubstrateNode substrateNode, VirtualNode virtualNode) {
		// �ж��ܱߵ���·��Դ�Ƿ������������·����
		// ģ�����
		PriorityQueue<Double> priorityQueueSubstrate = new PriorityQueue<>(Collections.reverseOrder()); // ����ײ���·
		PriorityQueue<Double> priorityQueueVirtual = new PriorityQueue<>(Collections.reverseOrder()); // ����������·
		for (VirtualLink vl : virtualNetwork.getOutEdges(virtualNode)) {
			priorityQueueVirtual.offer(Utils.getBandwith(vl));
		}
		for (SubstrateLink sl : substrateNetwork.getOutEdges(substrateNode)) {
			priorityQueueSubstrate.offer(Utils.getBandwith(sl));
		}
		
		// ��������������������Ĵ�����Դʱ���˳�ѭ��
		while (!priorityQueueVirtual.isEmpty()) {
			Double demand = priorityQueueVirtual.poll();
			Double resource = priorityQueueSubstrate.poll();
			if (Utils.greatEqual(resource, demand)) {
				// ����
				priorityQueueSubstrate.offer(resource - demand);
			} else {
				// ������Ҫ��
				return false;
			}
		}
		// ����Ҫ��
		return true;
	}
}
