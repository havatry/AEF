package vnreal.algorithms.myAEF.save;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import mulavito.algorithms.AbstractAlgorithmStatus;
import vnreal.algorithms.AbstractAlgorithm;
import vnreal.algorithms.AlgorithmParameter;
import vnreal.algorithms.isomorphism.SubgraphIsomorphismStackAlgorithm;
import vnreal.algorithms.myAEF.util.FileHelper;
import vnreal.algorithms.myAEF.util.GenerateGraph;
import vnreal.algorithms.myAEF.util.SummaryResult;
import vnreal.algorithms.myAEF.util.Utils;
import vnreal.constraints.resources.BandwidthResource;
import vnreal.constraints.resources.CpuResource;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;

/**
 * 按照泊松分布生成虚拟请求的开始时间，指数分布生成虚拟请求的停留时间，1.每隔一段时间统计所有请求映射的总时间
 * 2.每隔一段时间统计代价/收益比 3.每隔一段时间统计接收率。 对比三种算法
 * 2019年11月24日 下午7:46:27
 */
public class Main {
	private static final int interval = 50; // 实验处理间隔
	private static final int end = 2000; // 模拟实验时间
	private static final double arive_lambda = 3.0 / 100; // lambda1
	private static final double preserve_lambda = 1.0 / 500; // lambda2
	private static final int processed = -1; // -1表示该虚拟请求已经释放了或者没有映射成功, 无需释放
	private List<Integer> startList;
	private List<Integer> endList;
	//-------------// 初始化
	private List<VirtualNetwork> virtualNetworks = new ArrayList<VirtualNetwork>(); // 处理过的虚拟请求
	//-------------// 统计变量
	private double hasGainRevenue; // 上次为止获取的收益
	private int hasMappedSuccRequest; // 上次已经完成映射个数
	private long hasExecuteTime; // 上次算法已经计算时间
	private SummaryResult summaryResult = new SummaryResult();
	
	public static void main(String[] args) {
		Main main = new Main();
		main.init();
		main.process(new SubgraphIsomorphismStackAlgorithm(initParam()), initProperty());
//		System.out.println("print before");
		System.out.println(main.summaryResult);
	}
	
	// 生成虚拟请求的到达时间和停留时间
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
		// 固定从文件中读取
		NetworkStack networkStack = FileHelper.readFromXml("results/file/substratework_20191130135939.xml");
		SubstrateNetwork substrateNetwork = networkStack.getSubstrate();
//		FileHelper.writeToXml(Constants.FILE_NAME, new NetworkStack(substrateNetwork, null));
		// 每隔50 time unit进行处理一次
		int inter = 0; // 下次处理的开始位置, 指示器
		for (int time = interval; time <= end; time += interval) {
			// 处理endList
			processEndList(time, inter);
			// 处理新的strtList
			for (int i = inter; i < startList.size(); i++) {
				if (startList.get(i) <= time) {
					// 需要处理
					// 生成虚拟拓扑
					VirtualNetwork virtualNetwork;
					do {
						virtualNetwork= generateGraph.generateVNet();
					} while (!Utils.isConnected(virtualNetwork));
					//-----------// 添加虚拟网络否则npe
					virtualNetworks.add(virtualNetwork);
					// 调用算法去处理
					algorithm.setStack(new NetworkStack(substrateNetwork, Arrays.asList(virtualNetwork)));
					algorithm.performEvaluation();
					// 获取信息
					List<AbstractAlgorithmStatus> status = algorithm.getStati();
					// 第一项是映射成功率 第二项是执行时间 第三项是收益
					if (status.get(0).getRatio() == 100) {
						hasMappedSuccRequest++;
						hasGainRevenue += (Double)status.get(2).getValue();
//						System.out.println("i = " + i + ": mapped succ");
					} else {
						// 撤销
						startList.set(i, processed);
					}
					hasExecuteTime += (Long)status.get(1).getValue();
				} else {
					break; // next time
				}
				inter++;
			}
			//-----------------------// 统计
//			summaryResult.addRevenueToTime(hasGainRevenue / (inter * interval));
			summaryResult.addRevenueToTime(hasGainRevenue / time);
			summaryResult.addTotaTime(hasExecuteTime);
			summaryResult.addVnAcceptance((double)hasMappedSuccRequest / inter);
			// 获取底层网络代价
			double nodeOcc = 0.0, linkOcc = 0.0;
			for (SubstrateNode sn : substrateNetwork.getVertices()) {
				// 占用的资源
				nodeOcc += ((CpuResource)sn.get().get(0)).getOccupiedCycles();
			}
			for (SubstrateLink sl : substrateNetwork.getEdges()) {
				// 占用的带宽
				linkOcc += ((BandwidthResource)sl.get().get(0)).getOccupiedBandwidth();
			}
			double cost = nodeOcc  + linkOcc;
			summaryResult.addCostToRevenue(cost / hasGainRevenue); // 调整顺序
		}
	}
	
	private void processEndList(int time, int inter) {
		for (int i = 0; i < inter; i++) {
			if (startList.get(i) == processed) {
				// 不用处理
				continue;
			}
			if (startList.get(i) >= time) {
				// 无需处理, 因为间隔时间至少是1 time unit
				break;
			}
			if (startList.get(i) + endList.get(i) <= time) {
				// 在当前时刻之前需要释放
				virtualNetworks.get(i).clearVnrMappings();
				startList.set(i, processed);
			}
		}
	}
	
	private static AlgorithmParameter initParam() {
		AlgorithmParameter algorithmParameter = new AlgorithmParameter();
		algorithmParameter.put("linkMapAlgorithm", "bfs");
		algorithmParameter.put("distance", "70");
		algorithmParameter.put("advanced", "false");
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
