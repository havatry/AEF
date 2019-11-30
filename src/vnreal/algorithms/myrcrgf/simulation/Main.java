package vnreal.algorithms.myrcrgf.simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import mulavito.algorithms.AbstractAlgorithmStatus;
import vnreal.algorithms.AbstractAlgorithm;
import vnreal.algorithms.AlgorithmParameter;
import vnreal.algorithms.myrcrgf.strategies.RCRGF2Algorithm;
import vnreal.algorithms.myrcrgf.util.Constants;
import vnreal.algorithms.myrcrgf.util.FileHelper;
import vnreal.algorithms.myrcrgf.util.GenerateGraph;
import vnreal.algorithms.myrcrgf.util.SummaryResult;
import vnreal.algorithms.myrcrgf.util.Utils;
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
public class Main {
	private static final int interval = 50; // ʵ�鴦����
	private static final int end = 2000; // ģ��ʵ��ʱ��
	private static final double arive_lambda = 3.0 / 100; // lambda1
	private static final double preserve_lambda = 1.0 / 500; // lambda2
	private static final int processed = -1; // -1��ʾ�����������Ѿ��ͷ��˻���û��ӳ��ɹ�, �����ͷ�
	private List<Integer> startList;
	private List<Integer> endList;
	//-------------// ��ʼ��
	private List<VirtualNetwork> virtualNetworks = new ArrayList<VirtualNetwork>(); // ���������������
	//-------------// ͳ�Ʊ���
	private double hasGainRevenue; // �ϴ�Ϊֹ��ȡ������
	private int hasMappedSuccRequest; // �ϴ��Ѿ����ӳ�����
	private long hasExecuteTime; // �ϴ��㷨�Ѿ�����ʱ��
	private SummaryResult summaryResult = new SummaryResult();
	
	public static void main(String[] args) {
		Main main = new Main();
		main.init();
		main.process(new RCRGF2Algorithm(initParam()), initProperty());
//		System.out.println("print before");
		System.out.println(main.summaryResult);
	}
	
	// ������������ĵ���ʱ���ͣ��ʱ��
	private void init() {
		startList = new ArrayList<Integer>();
		endList = new ArrayList<>();
		startList.add(Utils.exponentialDistribution(arive_lambda));
		endList.add(Utils.exponentialDistribution(preserve_lambda));
		
		while (startList.get(startList.size() - 1) <= end) {
			startList.add(startList.get(startList.size() - 1) + Utils.exponentialDistribution(arive_lambda));
			endList.add(Utils.exponentialDistribution(preserve_lambda));
		}
		startList.remove(startList.size() - 1);
		endList.remove(endList.size() - 1);
	}
	
	private void process(AbstractAlgorithm algorithm, Properties properties) {
		GenerateGraph generateGraph = new GenerateGraph(properties);
//		SubstrateNetwork substrateNetwork = generateGraph.generateSNet();
		// �̶����ļ��ж�ȡ
		NetworkStack networkStack = FileHelper.readFromXml("results/file/substratework_20191130135939.xml");
		SubstrateNetwork substrateNetwork = networkStack.getSubstrate();
//		FileHelper.writeToXml(Constants.FILE_NAME, new NetworkStack(substrateNetwork, null));
		// ÿ��50 time unit���д���һ��
		int inter = 0; // �´δ���Ŀ�ʼλ��, ָʾ��
		for (int time = interval; time <= end; time += interval) {
			// ����endList
			processEndList(time, inter);
			// �����µ�strtList
			for (int i = inter; i < startList.size(); i++) {
				if (startList.get(i) <= time) {
					// ��Ҫ����
					// ������������
					VirtualNetwork virtualNetwork;
					do {
						virtualNetwork= generateGraph.generateVNet();
					} while (!Utils.isConnected(virtualNetwork));
					//-----------// ��������������npe
					virtualNetworks.add(virtualNetwork);
					// �����㷨ȥ����
					algorithm.setStack(new NetworkStack(substrateNetwork, Arrays.asList(virtualNetwork)));
					algorithm.performEvaluation();
					// ��ȡ��Ϣ
					List<AbstractAlgorithmStatus> status = algorithm.getStati();
					// ��һ����ӳ��ɹ��� �ڶ�����ִ��ʱ�� ������������
					if (status.get(0).getRatio() == 100) {
						hasMappedSuccRequest++;
						hasGainRevenue += (Double)status.get(2).getValue();
//						System.out.println("i = " + i + ": mapped succ");
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
		//-----------//
		return algorithmParameter;
	}
	
	private static Properties initProperty() {
		Properties properties = new Properties();
		properties.put("snNodes", "10");
		properties.put("minVNodes", "5");
		properties.put("maxVNodes", "6");
		properties.put("snAlpha", "0.5");
		properties.put("vnAlpha", "0.5");
		//---------//
		return properties;
	}
}
