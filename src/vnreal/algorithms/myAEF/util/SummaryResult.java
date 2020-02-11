package vnreal.algorithms.myAEF.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
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
    private List<Double> bandwidthStandardDiff; // ÿ��ʱ���ײ��������ı�׼��
	
	public SummaryResult() {
		// TODO Auto-generated constructor stub
		totalTime = new ArrayList<Long>();
		vnAcceptance = new ArrayList<Double>();
		costToRevenue = new ArrayList<Double>();
		revenueToTime = new ArrayList<Double>();
		bandwidthStandardDiff = new ArrayList<>();
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

	public void addBandwidthStandardDiff(double bsd) {
	    bandwidthStandardDiff.add(bsd);
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

    public List<Double> getBandwidthStandardDiff() {
        return bandwidthStandardDiff;
    }

    @Override
    public String toString() {
        return "SummaryResult{" +
                "totalTime=" + totalTime +
                ", vnAcceptance=" + vnAcceptance +
                ", costToRevenue=" + costToRevenue +
                ", revenueToTime=" + revenueToTime +
                ", bandwidthStandardDiff=" + bandwidthStandardDiff +
                '}';
    }

    public void writeToFile(String filename) {
		try {
			PrintWriter out = new PrintWriter(filename);
			List<Long> time = getTotalTime();
			for (Long l : time) {
				out.print(l);
				out.print(" ");
			}
			out.println();
			List<Double> ac = getVnAcceptance();
			for (Double d : ac) {
				out.print(d);
				out.print(" ");
			}
			out.println();
			List<Double> cr = getCostToRevenue();
			for (Double d2 : cr) {
				out.print(d2);
				out.print(" ");
			}
			out.println();
			List<Double> rt = getRevenueToTime();
			for (Double d3 : rt) {
				out.print(d3);
				out.print(" ");
			}
            out.println();
			List<Double> bsd = getBandwidthStandardDiff();
			for (Double d4 : bsd) {
			    out.print(d4);
			    out.print(" ");
            }
			out.println();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
