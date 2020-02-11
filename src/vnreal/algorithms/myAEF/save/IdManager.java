package vnreal.algorithms.myAEF.save;

import java.util.HashMap;

import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNode;

/**
 * �ײ�ڵ����·��id��Ӧ
 * 2019��11��17�� ����7:08:42
 */
public class IdManager {
	public static HashMap<Long, SubstrateNode> nodes; // �������ط�����
	public static HashMap<Long, SubstrateLink> links;
	
	public static Long getIndexForNode(SubstrateNode sn) {
		return sn.getId();
	}
	
	public static Long getIndexForLink(SubstrateLink sl) {
		return sl.getId();
	}
	
	public static SubstrateNode getNodeForIndex(Integer index) {
		return nodes.get(index);
	}
	
	public static SubstrateLink getLinkForIndex(Integer index) {
		return links.get(index);
	}
}
