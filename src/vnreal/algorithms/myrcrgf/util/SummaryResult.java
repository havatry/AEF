package vnreal.algorithms.myrcrgf.util;

import java.util.ArrayList;
import java.util.List;

/**
 * ��������װ
 * @author hudedong
 *
 */
public class SummaryResult {
	//-----------// ��ʼ��
	private List<Long> totalTime; // ����ִ�е�ÿ��ʱ������ʱ��
	private List<Double> vnAcceptance; // ÿ��ʱ�������������
	private List<Double> costToRevenue; // ÿ��ʱ���Ĵ��������
	private List<Double> revenueToTime; // ÿ��ʱ�������ʱ���
	
	public SummaryResult() {
		// TODO Auto-generated constructor stub
		totalTime = new ArrayList<Long>();
		vnAcceptance = new ArrayList<Double>();
		costToRevenue = new ArrayList<Double>();
		revenueToTime = new ArrayList<Double>();
	}
	
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

	@Override
	public String toString() {
		return "SummaryResult [totalTime=" + totalTime + ", vnAcceptance=" + vnAcceptance + ", costToRevenue="
				+ costToRevenue + ", revenueToTime=" + revenueToTime + "]";
	}
}
