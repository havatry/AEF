package vnreal.generators.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import tests.generators.GeneratorParameter;
import vnreal.constraints.resources.CpuResource;
import vnreal.core.oldFramework.ConversionHelper;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

/**
 * This Resource generator generating {@link CpuResource} is using a random generator 
 *  
 * @author Fabian Kokot
 *
 */
@GeneratorParameter(
		parameters = { "Networks:Networks", "TR:Min_CPU_Res", "TR:Max_CPU_Res"}
		)
public class RandomCpuResourceGenerator extends AbstractResourceGenerator<List<CpuResource>> {

	@Override
	public List<CpuResource> generate(ArrayList<Object> parameters) {
		ArrayList<CpuResource> resList = new ArrayList<CpuResource>();
		
		NetworkStack ns = (NetworkStack)parameters.get(0);
		Integer minCPU = ConversionHelper.paramObjectToInteger(parameters.get(1));
		Integer maxCPU = ConversionHelper.paramObjectToInteger(parameters.get(2));
		
		Random random = new Random();
		
		
		SubstrateNetwork sn = ns.getSubstrate();
		
		for(SubstrateNode n : sn.getVertices()) {
			CpuResource cpu = new CpuResource(n);
			int value = (int) (minCPU + (maxCPU
					- minCPU + 1)
					* random.nextDouble());
			cpu.setCycles((double) value);
			n.add(cpu);
			resList.add(cpu);
		}
		
		return resList;
	}

	@Override
	public void reset() {
		
	}

}
