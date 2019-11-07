package vnreal.algorithms.rcrgf.test;

import org.junit.Before;
import org.junit.Test;

import vnreal.algorithms.rcrgf.config.Constants;
import vnreal.algorithms.rcrgf.core.RCRGFStackAlgorithm;
import vnreal.core.Scenario;
import vnreal.io.XMLImporter;
import vnreal.network.NetworkStack;

public class TestRCRGF {
	private static NetworkStack networkStack;
	
	@Before
	public void start() {
		String filename = "topology_10_5_1_0.1.xml";
		Scenario scenario = XMLImporter.importScenario(Constants.WRITE_RESOURCE + filename);
		networkStack = scenario.getNetworkStack();
	}
	
	@Test // �����㷨����
	public void test01() {
		RCRGFStackAlgorithm argfStackAlgorithm = new RCRGFStackAlgorithm(networkStack);
		argfStackAlgorithm.performEvaluation();
	}
}