package vnreal.algorithms.myrcrgf.util;

import vnreal.io.XMLExporter;
import vnreal.io.XMLImporter;
import vnreal.network.NetworkStack;

/**
 * ���ɵ��ļ����Ա���
 * @author hudedong
 *
 */
public class FileHelper {
	public static void writeToXml(String filename, NetworkStack networkStack) {
		XMLExporter.exportStack(filename, networkStack);
	}
	
	public static NetworkStack readFromXml(String filename) {
		return XMLImporter.importScenario(filename).getNetworkStack();
	}
}
