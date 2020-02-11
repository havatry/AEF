package vnreal.algorithms.myAEF.strategies.nrm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vnreal.algorithms.AbstractNodeMapping;
import vnreal.algorithms.myAEF.util.Utils;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

import java.util.*;

public class NodeMappingNRM extends AbstractNodeMapping{
    private Logger log = LoggerFactory.getLogger(NodeMappingNRM.class);

    public NodeMappingNRM(boolean subsNodeOverload) {
        super(subsNodeOverload);
    }

    @Override
    protected boolean nodeMapping(SubstrateNetwork sNet, VirtualNetwork vNet) {
        PriorityQueue<VirtualNode> priorityQueueVirtual = new PriorityQueue<>((o1, o2) -> {
            //Created method stubs
            if (Utils.great(o1.getReferencedValue(), o2.getReferencedValue())) {
                return -1; // ����
            } else if (Utils.small(o1.getReferencedValue(), o2.getReferencedValue())) {
                return 1;
            } else {
                return 0;
            }
        });
        List<SubstrateNode> linkSubstrate = new ArrayList<>();

        // 1.�������������referenced value, ��Щ�����ڽڵ���
        for (VirtualNode vn : vNet.getVertices()) {
            vn.setReferencedValue(computeReferencedValueForVirtual(vNet, vn));
            priorityQueueVirtual.offer(vn);
        }
        for (SubstrateNode sn : sNet.getVertices()) {
            sn.setReferencedValue(computeReferencedValueForSubstrate(sNet, sn));
            linkSubstrate.add(sn);
        }
        Collections.sort(linkSubstrate, (o1, o2) -> {
            if (Utils.great(o1.getReferencedValue(), o2.getReferencedValue())) {
                return -1; // ����
            } else if (Utils.small(o1.getReferencedValue(), o2.getReferencedValue())) {
                return 1;
            } else {
                return 0;
            }
        });

        Set<SubstrateNode> mapped = new HashSet<>();
        while (!priorityQueueVirtual.isEmpty()) {
            // ����������ڵ���Ҫӳ���ʱ��
            VirtualNode virtualNode = priorityQueueVirtual.poll();
            boolean matched = false;
            for (SubstrateNode substrateNode : linkSubstrate) {
                // �Ƚ��Ƿ�����cpuԼ��
                if (!mapped.contains(substrateNode)
                        && Utils.smallEqual(Utils.getCpu(virtualNode), Utils.getCpu(substrateNode))) {
                    // ��ô��ƥ������
                    matched = true;
                    mapped.add(substrateNode); // �Ѿ�ӳ��
                    NodeLinkAssignation.vnm(virtualNode, substrateNode);
                    nodeMapping.put(virtualNode, substrateNode);
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    private double computeReferencedValueForVirtual(VirtualNetwork vNet, VirtualNode vn) {
        double result = 0.0;
        double cpuDemand = Utils.getCpu(vn);

        for (VirtualLink vl : vNet.getOutEdges(vn)) {
            result += 2 * cpuDemand * Utils.getBandwith(vl); // ���������п���storage capacity��cpu capacity, �������
                                                            // ������ͬ ��ͬ���仯���ʶ��������2, �����������ͬ��
        }

        return result;
    }

    private double computeReferencedValueForSubstrate(SubstrateNetwork sNet, SubstrateNode sn) {
        double result = 0.0;
        double cpuResource = Utils.getCpu(sn);

        for (SubstrateLink sl : sNet.getOutEdges(sn)) {
            result += 2 * cpuResource * Utils.getBandwith(sl);
        }

        return result;
    }
}
