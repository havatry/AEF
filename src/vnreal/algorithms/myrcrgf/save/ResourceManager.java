package vnreal.algorithms.myrcrgf.save;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import vnreal.algorithms.myrcrgf.util.Utils;
import vnreal.constraints.resources.BandwidthResource;
import vnreal.constraints.resources.CpuResource;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNode;

/**
 * ����������������ռ����Դ�ͳ�����Դ
 * 2019��11��17�� ����6:42:54
 */
public class ResourceManager {
	private HashMap<Integer, List<String>> use;
	
	private static ResourceManager rm;
	
	private ResourceManager() {}
	
	// ����ģʽ
	public static ResourceManager getInstance() {
		if (rm == null) {
			// ���̻߳����� ������˫����
			rm = new ResourceManager();
		}
		return rm;
	}
	
	/**
	 * ��¼ÿ�����������ڵײ�������ʹ�õ���Դ����
	 * @param id ��������id
	 * @param req ������Դ���ַ�����ʾ����ʽ����ڵ� = cpuռ���� & ������· = ����ռ����
	 * @return �����Ƿ���Ա��ײ���������
	 */
	public boolean request(Integer id, String req) {
		if (use.get(id) == null) {
			use.put(id, new ArrayList<String>());
		}
		use.get(id).add(req);
		return operateResource(req, true);
	}
	
	/**
	 * �ͷ�һ��������������ռ�õ���Դ
	 * @param id ��������id
	 */
	public void free(Integer id) {
		// ��ȡ��Ӧ��������ռ�õ�������Դ
		List<String> usage = use.get(id);
		for (String info : usage) {
			operateResource(info, false);
		}
	}
	
	// ��������
	private boolean operateResource(String req, boolean add) {
		String[] parts = req.split("&");
		String[] nodeInfo = parts[0].split("=");
		String[] linkInfo = parts[1].split("=");
		// find node
		SubstrateNode selectedNode = IdManager.getNodeForIndex(Integer.parseInt(nodeInfo[0]));
		CpuResource cpuResource = (CpuResource)selectedNode.get().get(0);
		cpuResource.setOccupiedCycles(cpuResource.getOccupiedCycles() +
				(add ? Double.parseDouble(nodeInfo[1]) : -Double.parseDouble(nodeInfo[1])));
		// find link
		SubstrateLink selectedLink = IdManager.getLinkForIndex(Integer.parseInt(linkInfo[0]));
		BandwidthResource bandwidthResource = (BandwidthResource) selectedLink.get().get(0);
		bandwidthResource.setOccupiedBandwidth(cpuResource.getOccupiedCycles() +
				(add ? Double.parseDouble(linkInfo[1]) : -Double.parseDouble(linkInfo[1])));
		return !add || Utils.greatEqual(cpuResource.getCycles(), cpuResource.getOccupiedCycles()) && 
				Utils.greatEqual(bandwidthResource.getBandwidth(), bandwidthResource.getOccupiedBandwidth());
	}
	
	/**
	 * ����һ����������ռ�õĵײ���Դ����
	 * @param id ��������id
	 * @return ռ����Դ��
	 */
	public Integer fulfill(Integer id) {
		Integer result = 0;
		for (String req : use.get(id)) {
			String[] parts = req.split("&");
			String[] nodeInfo = parts[0].split("=");
			String[] linkInfo = parts[1].split("=");
			result += Integer.parseInt(nodeInfo[1]) + Integer.parseInt(linkInfo[1]);
		}
		return result;
	}
}
