package vnreal.algorithms.myrcrgf.simulation;

import java.util.Arrays;
import java.util.List;
import mulavito.algorithms.AbstractAlgorithmStatus;
import vnreal.algorithms.AbstractAlgorithm;
import vnreal.algorithms.AlgorithmParameter;
import vnreal.algorithms.AvailableResources;
import vnreal.algorithms.isomorphism.SubgraphIsomorphismStackAlgorithm;
import vnreal.algorithms.myrcrgf.strategies.RCRGF2Algorithm;
import vnreal.algorithms.myrcrgf.util.FileHelper;
import vnreal.algorithms.myrcrgf.util.SummaryResult;
import vnreal.constraints.resources.BandwidthResource;
import vnreal.constraints.resources.CpuResource;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;

/**
 * ���ղ��ɷֲ�������������Ŀ�ʼʱ�䣬ָ���ֲ��������������ͣ��ʱ�䣬1.ÿ��һ��ʱ��ͳ����������ӳ�����ʱ��
 * 2.ÿ��һ��ʱ��ͳ�ƴ���/����� 3.ÿ��һ��ʱ��ͳ�ƽ����ʡ� �Ա������㷨
 * 2019��11��24�� ����7:46:27
 */
public class Run {
	private static final int interval = 50; // ʵ�鴦����
	private static final int end = 2000; // ģ��ʵ��ʱ��
	private static final int processed = -1; // -1��ʾ�����������Ѿ��ͷ��˻���û��ӳ��ɹ�, �����ͷ�
	private List<Integer> startList;
	private List<Integer> endList;
	//-------------// ��ʼ��
	private List<VirtualNetwork> virtualNetworks; // ���������������
	//-------------// ͳ�Ʊ���
	private double hasGainRevenue; // �ϴ�Ϊֹ��ȡ������
	private int hasMappedSuccRequest; // �ϴ��Ѿ����ӳ�����
	private long hasExecuteTime; // �ϴ��㷨�Ѿ�����ʱ��
	private SummaryResult summaryResult = new SummaryResult();
	
	public static void main(String[] args) {
		String base = "results/file/";
		String filename = base + "substratework_20191201153422.xml";
		AlgorithmParameter parameter = initParam();
		new Run().process(new RCRGF2Algorithm(parameter), filename);
		new Run().process(new AvailableResources(parameter), filename);
		new Run().process(new SubgraphIsomorphismStackAlgorithm(parameter), filename);
		System.out.println("Done");
	}
	
	@SuppressWarnings("unchecked")
	private void process(AbstractAlgorithm algorithm, String filename) {
		
		Object[] result = FileHelper.readContext(filename);
		SubstrateNetwork substrateNetwork = ((NetworkStack)result[0]).getSubstrate();
		System.out.println(substrateNetwork.getEdgeCount());
		virtualNetworks = ((NetworkStack)result[0]).getVirtuals();
		startList = (List<Integer>)result[1];
		endList = (List<Integer>)result[2];
		
		// ÿ��50 time unit���д���һ��
		int inter = 0; // �´δ���Ŀ�ʼλ��, ָʾ��
		for (int time = interval; time <= end; time += interval) {
			// ����endList
			processEndList(time, inter);
			// �����µ�strtList
			for (int i = inter; i < startList.size(); i++) {
				if (startList.get(i) <= time) {
					// �����㷨ȥ����
					algorithm.setStack(new NetworkStack(substrateNetwork, Arrays.asList(virtualNetworks.get(i))));
					algorithm.performEvaluation();
					// ��ȡ��Ϣ
					List<AbstractAlgorithmStatus> status = algorithm.getStati();
					// ��һ����ӳ��ɹ��� �ڶ�����ִ��ʱ�� ������������
					if (status.get(0).getRatio() == 100) {
						hasMappedSuccRequest++;
						hasGainRevenue += (Double)status.get(2).getValue();
					} else {
						// ����
						startList.set(i, processed);
					}
					hasExecuteTime += (Long)status.get(1).getValue();
				} else {
					break; // next time
				}
				inter++;
			}
			//-----------------------// ͳ��
//			summaryResult.addRevenueToTime(hasGainRevenue / (inter * interval));
			summaryResult.addRevenueToTime(hasGainRevenue / time);
			summaryResult.addTotaTime(hasExecuteTime);
			summaryResult.addVnAcceptance((double)hasMappedSuccRequest / inter);
			// ��ȡ�ײ��������
			double nodeOcc = 0.0, linkOcc = 0.0;
			for (SubstrateNode sn : substrateNetwork.getVertices()) {
				// ռ�õ���Դ
				nodeOcc += ((CpuResource)sn.get().get(0)).getOccupiedCycles();
			}
			for (SubstrateLink sl : substrateNetwork.getEdges()) {
				// ռ�õĴ���
				linkOcc += ((BandwidthResource)sl.get().get(0)).getOccupiedBandwidth();
			}
			double cost = nodeOcc  + linkOcc;
			summaryResult.addCostToRevenue(cost / hasGainRevenue); // ����˳��
		}
		// ������ļ�
		String fix;
		if (algorithm instanceof RCRGF2Algorithm) {
			fix = "rcrgf";
		} else if (algorithm instanceof AvailableResources) {
			fix = "greedy";
		} else if (algorithm instanceof SubgraphIsomorphismStackAlgorithm) {
			fix = "subgraph";
		} else {
			fix = "null";
		}
		String writeFileName = filename.replace("file", "out").substring(0, filename.lastIndexOf(".") - 1) + "_" + fix + ".txt";
		summaryResult.writeToFile(writeFileName);
	}
	
	private void processEndList(int time, int inter) {
		for (int i = 0; i < inter; i++) {
			if (startList.get(i) == processed) {
				// ���ô���
				continue;
			}
			if (startList.get(i) >= time) {
				// ���账��, ��Ϊ���ʱ��������1 time unit
				break;
			}
			if (startList.get(i) + endList.get(i) <= time) {
				// �ڵ�ǰʱ��֮ǰ��Ҫ�ͷ�
				virtualNetworks.get(i).clearVnrMappings();
				startList.set(i, processed);
			}
		}
	}
	
	private static AlgorithmParameter initParam() {
		AlgorithmParameter algorithmParameter = new AlgorithmParameter();
		algorithmParameter.put("linkMapAlgorithm", "bfs");
		algorithmParameter.put("distance", "30");
		algorithmParameter.put("advanced", "false");
		//-----------//
		return algorithmParameter;
	}
}
