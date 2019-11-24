package vnreal.algorithms.myrcrgf.control;

import java.util.TreeMap;

/**
 * �������ݷ���
 * 2019��11��17�� ����8:32:01
 */
public class DataManager {
	private TreeMap<Integer, Double> timeAve; // value ��λms
	private TreeMap<Integer, Integer> revenueUse; // value
	private TreeMap<Integer, Integer> costUse; // ���¼���
	private TreeMap<Integer, Double> acceptanceAve; // 0����1
	
	public String report(Integer timeUnit) {
		// ���浱ǰʱ�̵�����
		StringBuilder stringBuilder = new StringBuilder("timeAve=");
		stringBuilder.append(timeAve.floorEntry(timeUnit).getValue());
		stringBuilder.append("&");
		stringBuilder.append("revenueUse=");
		stringBuilder.append(revenueUse.floorEntry(timeUnit).getValue());
		stringBuilder.append("&");
		stringBuilder.append("costUse=");
		stringBuilder.append(costUse.floorEntry(timeUnit).getValue());
		stringBuilder.append("&");
		stringBuilder.append("acceptanceAve=");
		stringBuilder.append(acceptanceAve.floorEntry(timeUnit).getValue());
		return stringBuilder.toString();
	}
	
	public void update(String info) {
		// �ź� ʱ�� value
		String[] part = info.split(" ");
		Integer timeUnit = Integer.parseInt(part[1]);
		switch (part[0]) {
			case "timeAve":
				Double newValue = (timeAve.floorEntry(timeUnit).getValue() * timeAve.size() + Integer.parseInt(part[2])) / (timeAve.size() + 1);
				timeAve.put(timeUnit, newValue);
				break;
			case "revenueUse":
				Integer revenueValue = revenueUse.floorEntry(timeUnit).getValue() + Integer.parseInt(part[2]);
				revenueUse.put(timeUnit, revenueValue);
				break;
			case "costUse":
				// �������ط�����
				Integer costValue = costUse.floorEntry(timeUnit).getValue() + Integer.parseInt(part[2]); // ����������
				costUse.put(timeUnit, costValue);
				break;
			default:
				Double acceptanceValue = (acceptanceAve.floorEntry(timeUnit).getValue() 
						* acceptanceAve.size() + Integer.parseInt(part[2])) / (acceptanceAve.size() + 1);
				acceptanceAve.put(timeUnit, acceptanceValue);
				break;
		}
	}
}
