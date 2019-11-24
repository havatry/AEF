package vnreal.algorithms.myrcrgf.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * �������ļ��ж�ȡ���� ����
 * 2019��11��23�� ����9:50:04
 */
public class Constants {
	// ����Լ��D
	public static final Properties PROPERTIES;
	static {
		// �������ļ��ж�ȡ
		PROPERTIES = new Properties();
		try {
			PROPERTIES.load(new FileInputStream("results/setting/config.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static final boolean DIRECTED = false; // ����ͼ
	public static final boolean ASSURE_UNIQUE = false; // sample dijkstra
	public static final boolean ADAPTE_UNDIRECTEDGRAPH = true; // ��������ͼ
	public static final boolean HIDDEN_RIGHT_TAB = true; // ��ui�й�
	
}
