package vnreal.algorithms.myAEF.strategies;

import vnreal.algorithms.AlgorithmParameter;
import vnreal.algorithms.GenericMappingAlgorithm;
import vnreal.algorithms.linkmapping.kShortestPathLinkMapping;
import vnreal.algorithms.myAEF.strategies.aef.LinkMapping;
import vnreal.algorithms.myAEF.strategies.aef.LinkMapping2;
import vnreal.algorithms.myAEF.strategies.aef.NodeMapping;

public class AEFAlgorithm extends GenericMappingAlgorithm{
	// Default values
	private static final double DEFAULT_DISTANCE_CONSTRAINT = 70.0;
	private static final boolean DEFAULT_OVERLOAD = false;
	private static final int DEFAULT_KSP = 1;
	private static final boolean DEFAULT_EPPSTEIN = true;
	private static final String DEFAULT_LINKMAP_ALGORITHM = "ksp";
	private static final boolean DEFAULT_ADVANCED = false;
	private static final int DEFAULT_SPL = 2;
	private boolean advanced;

	public AEFAlgorithm(AlgorithmParameter param, boolean advanced) {
		double distanceConstraint = param.getDouble("distanceConstraint", DEFAULT_DISTANCE_CONSTRAINT);
		boolean nodeOverload = param.getBoolean("overload", DEFAULT_OVERLOAD);
		this.advanced = advanced;
		nodeMappingAlgorithm = new NodeMapping(distanceConstraint, nodeOverload);
		
		if (param.getString("linkMapAlgorithm", DEFAULT_LINKMAP_ALGORITHM).equals("ksp")) {
			int k = param.getInteger("ksp", DEFAULT_KSP);
			boolean eppstein = param.getBoolean("eppstein", DEFAULT_EPPSTEIN);
			linkMappingAlgorithm = new kShortestPathLinkMapping(k, eppstein);
		} else {
		    int SPL = param.getInteger("spl", DEFAULT_SPL);
            linkMappingAlgorithm = advanced ? new LinkMapping2(SPL) : new LinkMapping(); // 更新算法或者Baseline算法
		}
	}

    public boolean isAdvanced() {
        return advanced;
    }
}
