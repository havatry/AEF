/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2010-2011, The VNREAL Project Team.
 * 
 * This work has been funded by the European FP7
 * Network of Excellence "Euro-NF" (grant agreement no. 216366)
 * through the Specific Joint Developments and Experiments Project
 * "Virtual Network Resource Embedding Algorithms" (VNREAL). 
 *
 * The VNREAL Project Team consists of members from:
 * - University of Wuerzburg, Germany
 * - Universitat Politecnica de Catalunya, Spain
 * - University of Passau, Germany
 * See the file AUTHORS for details and contact information.
 * 
 * This file is part of ALEVIN (ALgorithms for Embedding VIrtual Networks).
 *
 * ALEVIN is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License Version 3 or later
 * (the "GPL"), or the GNU Lesser General Public License Version 3 or later
 * (the "LGPL") as published by the Free Software Foundation.
 *
 * ALEVIN is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * or the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License and
 * GNU Lesser General Public License along with ALEVIN; see the file
 * COPYING. If not, see <http://www.gnu.org/licenses/>.
 *
 * ***** END LICENSE BLOCK ***** */
package vnreal.algorithms.nodemapping;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import vnreal.algorithms.AbstractNodeMapping;
import vnreal.algorithms.utils.LpSolver;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.algorithms.utils.dataSolverFile;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.BandwidthDemand;
import vnreal.constraints.demands.CpuDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.core.Consts;
import vnreal.hiddenhopmapping.BandwidthCpuHiddenHopMapping;
import vnreal.hiddenhopmapping.IHiddenHopMapping;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class OptimalEnergyMappingHiddenHop extends AbstractNodeMapping {
	// PRIVATE variables
	private int distance; // Distance to select the candidate nodes to be
	// mapped.
	// Type variable indicates the rounding type
	private Map<VirtualNode, List<SubstrateNode>> candiNodes;
	boolean withDist;
	NetworkStack stack;
	private int maxSubsNodeGrade;
	private List<SubstrateNode> chosenAccessNodes = new LinkedList<SubstrateNode>();

	@Deprecated
	public OptimalEnergyMappingHiddenHop(SubstrateNetwork sNet, int dist,
			boolean nodeOverload, boolean withDistance, NetworkStack stack) {
		super(nodeOverload);
		this.distance = dist;
		if (!withDistance) { this.distance = -1; }
		this.stack = stack;
	}
	
	public OptimalEnergyMappingHiddenHop(int dist, boolean nodeOverload) {
		super(nodeOverload);
		this.distance = dist;
	}

	// Node and link mapping phase (in this case it is performed in
	// the same phase).
	@Override
	protected boolean nodeMapping(SubstrateNetwork sNet, VirtualNetwork vNet) {
		chosenAccessNodes = new LinkedList<SubstrateNode>();
		//candiNodes = createCandidateSet(vNet);
		// WITH CPLEX
		candiNodes = createCandidateSetWithType(sNet, vNet);
		///////////////////////////////
		stack.clearVnrMappings(vNet);
		Map<List<String>, Double> x;
		Map<List<String>, Double> flow;
		LpSolver problemSolver = new LpSolver();
		Random intGenerator = new Random();
		double hhFactor = 0;

		String dataFileName = Consts.LP_SOLVER_DATAFILE
				+ Integer.toString(intGenerator.nextInt(2001)) + ".dat";

		dataSolverFile lpNodeMappingData = new dataSolverFile(
				Consts.LP_SOLVER_FOLDER + dataFileName);

		for (IHiddenHopMapping hhCpuMapping : hhMappings) {
			if (hhCpuMapping instanceof BandwidthCpuHiddenHopMapping) {
				hhFactor = ((BandwidthCpuHiddenHopMapping) hhCpuMapping)
						.getFactor();
				break;
			}
		}

		lpNodeMappingData.createExactMipEnergySolverFileWithCost(sNet, vNet,
				candiNodes, hhFactor, maxSubsNodeGrade, false, 1, 0);
		problemSolver.solveMIP(Consts.LP_SOLVER_FOLDER,
				Consts.ILP_EXACTMAPPING_ENERGY_MODEL_HIDDEN_HOP_WITH_COST,
				dataFileName);

		if (!problemSolver.problemFeasible())
			return false;

		// x and flow are the variables of the MIP model indicating the
		// results of node and link mapping (refer to the Houdi paper)

		x = MiscelFunctions.processSolverResult(problemSolver.getX(), "x[]");
		flow = MiscelFunctions.processSolverResult(problemSolver.getFlow(),
				"flow[]");

		if (performNodeMapping(sNet, vNet, x)) {
			if (!performLinkMapping(sNet, vNet, flow)) {
				return false;
			}
		} else {
			return false;
		}

		return true;

		// TEMP testing of CPLEX solution

		/*SolveCplexScenario cplexSolution = new SolveCplexScenario(sNet, vNet,
				candiNodes, maxSubsNodeGrade, true);
		try {
			cplexSolution.solveProblem();
		} catch (IloException e) { // TODO
			e.printStackTrace();
		}
		if (performNodeMapping(vNet, cplexSolution.getx())) {
			if (!performLinkMapping(vNet, cplexSolution.getflow())) {
				return false;
			}
		} else {
			return false;
		}

		return true;*/

	}

	/**
	 * 
	 * @param vNet
	 * @return The set of substrate candidates for each virtual node of vNet
	 */
	@SuppressWarnings("unused")
	private Map<VirtualNode, List<SubstrateNode>> createCandidateSet(
			SubstrateNetwork sNet, VirtualNetwork vNet) {
		Map<VirtualNode, List<SubstrateNode>> candidateSet = new LinkedHashMap<VirtualNode, List<SubstrateNode>>();
		List<SubstrateNode> substrateSet;
		for (Iterator<VirtualNode> itt = vNet.getVertices().iterator(); itt
				.hasNext();) {
			substrateSet = new LinkedList<SubstrateNode>();
			VirtualNode currVnode = itt.next();
			if (nodeMapping.containsKey(currVnode)) {
				substrateSet.add(nodeMapping.get(currVnode));
			} else {
				substrateSet.addAll(findFulfillingNodes(sNet, currVnode));
			}
			candidateSet.put(currVnode, substrateSet);
		}
		return candidateSet;
	}

	/**
	 * 
	 * @param vNet
	 * @return The set of substrate candidates for each virtual node of vNet
	 */
	private Map<VirtualNode, List<SubstrateNode>> createCandidateSetWithType(
			SubstrateNetwork sNet, VirtualNetwork vNet) {
		Map<VirtualNode, List<SubstrateNode>> candidateSet = new LinkedHashMap<VirtualNode, List<SubstrateNode>>();
		List<SubstrateNode> substrateSet;
		for (Iterator<VirtualNode> itt = vNet.getVertices().iterator(); itt
				.hasNext();) {
			substrateSet = new LinkedList<SubstrateNode>();
			VirtualNode currVnode = itt.next();
			if (nodeMapping.containsKey(currVnode)) {
				substrateSet.add(nodeMapping.get(currVnode));
			} else {
				substrateSet.addAll(findFulfillingNodesWithType(sNet, currVnode));
			}
			candidateSet.put(currVnode, substrateSet);
		}
		return candidateSet;
	}

	/**
	 * 
	 * @param vNode
	 * @return The set of feasible candidate substrate nodes for the virtual
	 *         vNode
	 */

	private Collection<SubstrateNode> findFulfillingNodes(SubstrateNetwork sNet, VirtualNode vNode) {
		List<SubstrateNode> nodes = new LinkedList<SubstrateNode>();
		for (SubstrateNode n : sNet.getVertices()) {
			if (withDist) {
				if (nodeDistance(vNode, n, distance)) {
					for (AbstractResource res : n)
						for (AbstractDemand dem : vNode)
							if (res.accepts(dem) && res.fulfills(dem)) {
								nodes.add(n);
							}
				}
			} else {
				for (AbstractResource res : n)
					for (AbstractDemand dem : vNode)
						if (res.accepts(dem) && res.fulfills(dem)) {
							nodes.add(n);
						}
			}
		}
		return nodes;
	}

	/**
	 * 
	 * @param vNode
	 * @return The set of feasible candidate substrate nodes for the virtual
	 *         vNode
	 */
	private Collection<SubstrateNode> findFulfillingNodesWithType(
			SubstrateNetwork sNet, VirtualNode vNode) {
		List<SubstrateNode> nodes = new LinkedList<SubstrateNode>();
		int numberAccNodes = 10;
		if (vNode.getType().equals("Access")) {
			Random intGenerator = new Random();
			int randomNumber = intGenerator.nextInt(numberAccNodes);
			int i = 0;
			for (Iterator<SubstrateNode> accNodeIt = sNet.getVertices()
					.iterator(); accNodeIt.hasNext();) {
				SubstrateNode accNode = accNodeIt.next();
				if (accNode.getType().equals("Access")) {
					if (i == randomNumber) {
						boolean accept = true;
						if (withDist) {
							if (nodeDistance(vNode, accNode, distance)) {
								for (AbstractResource res : accNode)
									for (AbstractDemand dem : vNode)
										if (!(res.accepts(dem) && res
												.fulfills(dem)))
											accept = false;

							}
						} else {
							for (AbstractResource res : accNode)
								for (AbstractDemand dem : vNode)
									if (!(res.accepts(dem) && res.fulfills(dem)))
										accept = false;

						}
						if (accept && !chosenAccessNodes.contains(accNode)) {
							nodes.add(accNode);
							chosenAccessNodes.add(accNode);
							break;
						} else {
							randomNumber = intGenerator.nextInt(numberAccNodes);
							accNodeIt = sNet.getVertices().iterator();
							i = -1;

						}
					}
					i++;
				}
			}

		} else {
			for (SubstrateNode n : sNet.getVertices()) {
				if (n.getType().equals(vNode.getType())) {
					if (withDist) {
						if (nodeDistance(vNode, n, distance)) {
							for (AbstractResource res : n)
								for (AbstractDemand dem : vNode)
									if (res.accepts(dem) && res.fulfills(dem)) {
										nodes.add(n);
									}
						}
					} else {
						for (AbstractResource res : n)
							for (AbstractDemand dem : vNode)
								if (res.accepts(dem) && res.fulfills(dem)) {
									nodes.add(n);
								}
					}
				}
			}
		}

		return nodes;
	}

	/**
	 * Method to know if a substrate node is located in a distance less or equal
	 * than the predefined distance parameter
	 * 
	 * @param vNode
	 * @param sNode
	 * @param distance
	 * @return
	 */
	private boolean nodeDistance(VirtualNode vNode, SubstrateNode sNode,
			int distance) {
		double dis;
		dis = Math.pow(sNode.getCoordinateX() - vNode.getCoordinateX(), 2)
				+ Math.pow(sNode.getCoordinateY() - vNode.getCoordinateY(), 2);
		if (Math.sqrt(dis) <= distance) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method performs the node mapping taking into account the solver
	 * response in the variable x (containing the optimal node mapping)
	 * 
	 * @param vNet
	 * @param x
	 * @return boolean value indicating whether the node mapping has been
	 *         successful
	 */
	private boolean performNodeMapping(SubstrateNetwork sNet, VirtualNetwork vNet,
			Map<List<String>, Double> x) {
		List<String> tmpValues;
		VirtualNode currVnode;
		SubstrateNode currSnode;
		for (Iterator<List<String>> cad = x.keySet().iterator(); cad.hasNext();) {
			tmpValues = cad.next();
			if (x.get(tmpValues) == 1) {
				currVnode = getVnodeById(vNet,
						Integer.parseInt(tmpValues.get(0)));
				currSnode = getSnodeById(sNet, Integer.parseInt(tmpValues.get(1)));
				if (NodeLinkAssignation.vnm(currVnode, currSnode)) {
					if (!nodeMapping.containsKey(currVnode))
						nodeMapping.put(currVnode, currSnode);
				} else {
					throw new AssertionError(
							"Implementation mistake MIP model checks");
				}
			}
		}
		return true;
	}

	/**
	 * This method performs the link mapping taking into account the solver
	 * response in the variable flow (containing the optimal link mapping)
	 * 
	 * @param vNet
	 * @param flow
	 * @return boolean value indicating whether the link mapping has been
	 *         successful
	 */
	private boolean performLinkMapping(SubstrateNetwork sNet, VirtualNetwork vNet,
			Map<List<String>, Double> flow) {
		VirtualNode srcVnode, dstVnode;
		SubstrateNode corrSrcSNode, corrDstSNode, tempSrcSnode, tempDstSnode, hiddenHop = null;
		SubstrateLink tempSlink;
		double hhFactor = 0;
		BandwidthDemand newBwDem;
		CpuDemand tmpHhDemand = null;
		List<String> flowValues;
		Double tmpResult;
		Map<List<String>, Double> flowPercentages = findPercentages(flow, vNet);
		double VlOriginalDemand = 0;
		// Iterate all VirtualLinks on the current VirtualNetwork
		for (Iterator<VirtualLink> links = vNet.getEdges().iterator(); links
				.hasNext();) {
			VirtualLink tmpVlink = links.next();
			// Find the source and destiny of the current VirtualLink (tmpl)
			srcVnode = vNet.getSource(tmpVlink);
			dstVnode = vNet.getDest(tmpVlink);
			for (AbstractDemand dem : tmpVlink)
				if (dem instanceof BandwidthDemand) {
					VlOriginalDemand = ((BandwidthDemand) dem)
							.getDemandedBandwidth();
					break;
				}

			corrSrcSNode = nodeMapping.get(srcVnode);
			corrDstSNode = nodeMapping.get(dstVnode);
			if (!corrSrcSNode.equals(corrDstSNode)) {
				for (Iterator<List<String>> cad = flow.keySet().iterator(); cad
						.hasNext();) {
					flowValues = cad.next();
					tmpResult = MiscelFunctions.round(flow.get(flowValues), 12);
					if (tmpResult != 0) {
						if (corrSrcSNode.getId() == Integer.parseInt(flowValues
								.get(0))
								&& corrDstSNode.getId() == Integer
										.parseInt(flowValues.get(1))) {
							tempSrcSnode = getSnodeById(sNet,
									Integer.parseInt(flowValues.get(2)));
							tempDstSnode = getSnodeById(sNet,
									Integer.parseInt(flowValues.get(3)));
							tempSlink = sNet.findEdge(tempSrcSnode,
									tempDstSnode);
							// Create the new bandwidth demand that corresponds
							// to the percentage of BW in the solution of the
							// solver. tempSlink is part of the path mapping
							// tmpVlink

							newBwDem = new BandwidthDemand(tmpVlink);
							newBwDem.setDemandedBandwidth(MiscelFunctions
									.round(VlOriginalDemand	* flowPercentages.get(flowValues), 6));
							tmpVlink.add(newBwDem);
							// Getting the factor for the bandwidth to CPU
							// hidden hop mapping
							if (hhFactor == 0) {
								for (IHiddenHopMapping hh : hhMappings) {
									if (hh instanceof BandwidthCpuHiddenHopMapping)
										hhFactor = ((BandwidthCpuHiddenHopMapping) hh)
												.getFactor();

									break;
								}
							}
							hiddenHop = null;
							if (!tmpVlink.getHiddenHopDemands().isEmpty()) {
								// Hidden hops are considered, new hidden hop
								// demand should be created
								tmpHhDemand = (CpuDemand) new BandwidthCpuHiddenHopMapping(
										hhFactor).transform(newBwDem);
								tmpVlink.addHiddenHopDemand(tmpHhDemand);
								if (!sNet.getSource(tempSlink).equals(
										corrSrcSNode)) {
									hiddenHop = sNet.getSource(tempSlink);
								} else {
									hiddenHop = null;
								}
							}
							if (!NodeLinkAssignation
									.vlmSingleLink(tmpVlink, newBwDem,
											tempSlink, hiddenHop, tmpHhDemand))
								throw new AssertionError(
										"Some coding mistake, MIP model checks");
						}
					}
				}
			} else {
				// When source and destination nodes are the same, no mapping is
				// performed
			}

		}
		return true;
	}

	/**
	 * 
	 * @param vNet
	 * @param id
	 * @return the virtual node corresponding to the id
	 */
	private VirtualNode getVnodeById(VirtualNetwork vNet, int id) {
		for (Iterator<VirtualNode> itt = vNet.getVertices().iterator(); itt
				.hasNext();) {
			VirtualNode tempVirNode = itt.next();
			if (tempVirNode.getId() == id)
				return tempVirNode;
		}
		return null;
	}

	/**
	 * 
	 * @param id
	 * @return the substrate node corresponding to id
	 */
	private SubstrateNode getSnodeById(SubstrateNetwork sNet, int id) { // TODO: Find all instances of this and move to SubstrateNetwork
		for (SubstrateNode tempSubsNode : sNet.getVertices()) {
			if (tempSubsNode.getId() == id)
				return tempSubsNode;
		}
		return null;
	}

	private Map<List<String>, Double> findPercentages(
			Map<List<String>, Double> flow, VirtualNetwork vNet) {
		Map<List<String>, Double> newFlow = new LinkedHashMap<List<String>, Double>();
		Map<List<String>, Double> flowPercentages = new LinkedHashMap<List<String>, Double>();
		List<List<SubstrateNode>> mappedVLinks = new LinkedList<List<SubstrateNode>>();
		List<String> flowValues;
		SubstrateNode subNoSrc, subNoDst;
		double totalMappedFlow = 0;

		List<SubstrateNode> tempSubsNodePair = new LinkedList<SubstrateNode>();
		for (VirtualLink currVl : vNet.getEdges()) {
			tempSubsNodePair = new LinkedList<SubstrateNode>();
			tempSubsNodePair.add(nodeMapping.get(vNet.getSource(currVl)));
			tempSubsNodePair.add(nodeMapping.get(vNet.getDest(currVl)));
			if (!contains(mappedVLinks, tempSubsNodePair))
				mappedVLinks.add(tempSubsNodePair);
		}

		for (Iterator<List<String>> cad = flow.keySet().iterator(); cad
				.hasNext();) {
			flowValues = cad.next();
			if (MiscelFunctions.round(flow.get(flowValues), 12) != 0) {
				newFlow.put(flowValues, MiscelFunctions
						.round(flow.get(flowValues), 12));
			}
		}

		for (List<SubstrateNode> currNodePair : mappedVLinks) {
			subNoSrc = currNodePair.get(0);
			subNoDst = currNodePair.get(1);
			for (Iterator<List<String>> cad = newFlow.keySet().iterator(); cad
					.hasNext();) {
				flowValues = cad.next();
				if (subNoSrc.getId() == Integer.parseInt(flowValues.get(0))
						&& subNoDst.getId() == Integer.parseInt(flowValues
								.get(1))
						&& subNoSrc.getId() == Integer.parseInt(flowValues
								.get(2)))
					totalMappedFlow += newFlow.get(flowValues);
			}
			for (Iterator<List<String>> cad = newFlow.keySet().iterator(); cad
					.hasNext();) {
				flowValues = cad.next();
				if (subNoSrc.getId() == Integer.parseInt(flowValues.get(0))
						&& subNoDst.getId() == Integer.parseInt(flowValues
								.get(1))) {
					flowPercentages.put(
							flowValues,
							MiscelFunctions.round(newFlow.get(flowValues) / totalMappedFlow, 12));
				}
			}
			totalMappedFlow = 0;
		}
		return flowPercentages;
	}

	private boolean contains(List<List<SubstrateNode>> mappedVLinks,
			List<SubstrateNode> tempSubsNodePair) {
		for (List<SubstrateNode> currNodePair : mappedVLinks) {
			if (currNodePair.get(0).equals(tempSubsNodePair.get(0))
					&& currNodePair.get(1).equals(tempSubsNodePair.get(1)))
				return true;
		}

		return false;
	}

	public void setMaxSubsNodeGrade(int maxSubsNodeGrade) {
		this.maxSubsNodeGrade = maxSubsNodeGrade;
	}

}
