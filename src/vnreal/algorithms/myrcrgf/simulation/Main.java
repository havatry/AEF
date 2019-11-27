package vnreal.algorithms.myrcrgf.simulation;

import java.util.ArrayList;
import java.util.List;

import mulavito.algorithms.IAlgorithm;
import vnreal.algorithms.myrcrgf.util.Utils;
import vnreal.network.substrate.SubstrateNetwork;
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
	private List<VirtualNetwork> virtualNetworks; // ���������������
	
	public static void main(String[] args) {
		Main main = new Main();
		main.init();
		
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
	
	private void process(IAlgorithm algorithm) {
		// ÿ��50 time unit���д���һ��
		int inter = 0; // �´δ���Ŀ�ʼλ��, ָʾ��
		for (int time = interval; time <= end; time += interval) {
			// ����endList
			processEndList(time, inter);
			// �����µ�strtList
			for (int i = inter; i < startList.size(); i++) {
				if (startList.get(i) <= time) {
					// ��Ҫ����
					// ����Main����
				}
			}
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
	
//	private SubstrateNetwork produceSNet() {
//		
//	}
}
