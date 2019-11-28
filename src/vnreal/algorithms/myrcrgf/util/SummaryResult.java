package vnreal.algorithms.myrcrgf.util;

import java.util.List;

/**
 * ��������װ
 * @author hudedong
 *
 */
public class SummaryResult {
	private List<Long> totalTime; // ����ִ�е�ÿ��ʱ������ʱ��
	private List<Double> vnAcceptance; // ÿ��ʱ�������������
	private List<Double> costToRevenue; // ÿ��ʱ���Ĵ��������
	private List<Double> revenueToTime; // ÿ��ʱ�������ʱ���
	
	public void addTotaTime(long executionTime) {
		totalTime.add(executionTime);
	}
	
	// ֱ�Ӽ���� ��ӽ���
	public void addVnAcceptance(double vna) {
		vnAcceptance.add(vna);
	}
	
	public void addCostToRevenue(double ctr) {
		costToRevenue.add(ctr);
	}
	
	public void addRevenueToTime(double rtt) {
		revenueToTime.add(rtt);
	}
	
	public List<Double> getCostToRevenue() {
		return costToRevenue;
	}
	
	public List<Double> getRevenueToTime() {
		return revenueToTime;
	}
	
	public List<Long> getTotalTime() {
		return totalTime;
	}
	
	public List<Double> getVnAcceptance() {
		return vnAcceptance;
	}
}
