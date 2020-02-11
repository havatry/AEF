package vnreal.algorithms.myAEF.save;

import java.util.Iterator;
import java.util.TreeMap;

/**
 * ʱ����� ���ڹ���ÿ��ʱ��ڵ�Ĳ��������ⲿ���е���
 * 2019��11��17�� ����8:11:52
 */
public class TimeManager {
	private static TreeMap<Integer, Integer> freeTime; // ÿ������ĳ���ʱ��
	
	/**
	 * ����ǰ���󣬲�����ע�ᵽfreeTime�У��Ա��������ͷ�
	 * @param id ���������id
	 * @param endTime �����������ͷ�ʱ��
	 */
	public static void register(Integer id, Integer endTime) {
		freeTime.put(endTime, id);
		// ���ȴ���ǰ����
		// ί�и�ʵ���㷨����
		
		// �������� �����ʺ�ʱ�������
		// ����DataManager����
	}
	
	/**
	 * �ͷŵ�ǰtimeUnit֮ǰ����������
	 * @param timeUnit
	 */
	public static void filter(Integer timeUnit) {
		// ���˵�ǰʱ��֮ǰ����������
		Iterator<Integer> iterator = freeTime.keySet().iterator();
		while (iterator.hasNext()) {
			Integer key = iterator.next();
			if (key <= timeUnit) {
				// free��Դ
				ResourceManager.getInstance().free(freeTime.get(key));
				iterator.remove();
			} else {
				break;
			}
		}
		// ����cost
		// ����DataManager����
	}
}
