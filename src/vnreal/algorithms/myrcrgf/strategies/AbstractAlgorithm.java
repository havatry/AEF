package vnreal.algorithms.myrcrgf.strategies;

import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualNetwork;

/**
 * Ϊ��ͬ�㷨�ṩһ������ģ�壬ͳ���㷨����ʱ��ͽ��
 * 2019��11��19�� ����7:10:33
 */
public abstract class AbstractAlgorithm {
	private long executeTime; // �㷨ִ��ʱ��
	private boolean succ; // �Ƿ�ɹ�ӳ��
	
	public void wrap(SubstrateNetwork sn, VirtualNetwork vn) {
		long start = System.currentTimeMillis();
		succ = work(sn, vn);
		executeTime = System.currentTimeMillis() - start;
	}
	
	protected abstract boolean work(SubstrateNetwork sn, VirtualNetwork vn);
	
	public long getExecuteTime() {
		return executeTime;
	}
	
	public boolean isSucc() {
		return succ;
	}
}
