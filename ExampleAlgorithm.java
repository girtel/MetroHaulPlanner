import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.research.metrohaul.networkModel.WNet;
import com.net2plan.research.metrohaul.networkModel.WNode;
import com.net2plan.research.metrohaul.networkModel.WServiceChainRequest;
import com.net2plan.utils.InputParameter;
import com.net2plan.utils.Triple;

import java.util.List;
import java.util.Map;

public class ExampleAlgorithm implements IAlgorithm
{
	private InputParameter linerate_Gbps     = new InputParameter("linerate_Gbps", 40.0, "Linerate (in Gbps) per ligthpath", 1.0, true, 1000.0, true);
	private InputParameter slotsPerLightpath = new InputParameter("slotsPerLightpath", 1, "Number of occupied slots per lightpath", 1, Integer.MAX_VALUE);

	@Override
	public String executeAlgorithm(NetPlan netPlan, Map<String, String> algorithmParameters, Map<String, String> net2planParameters)
	{
		//First of all, initialize all parameters
		InputParameter.initializeAllInputParameterFieldsOfObject(this, algorithmParameters);

		WNet wNet = new WNet(netPlan);

		//Perform here initial checks

		List<WServiceChainRequest> scrList = wNet.getServiceChainRequests();
		for(WServiceChainRequest scr : scrList)
		{
			System.out.println(scr.getUserServiceName());
			List<String> vnfsToTraverse = scr.getSequenceVnfTypes();
			WNode originNode = scr.getPotentiallyValidOrigins().iterator().next();
//			WNode destinationNode = scr.getPotentiallyValidDestinations().it

			for(WNode node : scr.getPotentiallyValidDestinations())
				System.out.println("destination = " + node.getName());
			for(WNode node: scr.getPotentiallyValidOrigins())
				System.out.println("origin = "+node.getName());

//			System.out.println("origin = "+originNode.getName());
//			System.out.println("destination = "+destinationNode.getName());
//			List<List<? extends WAbstractNetworkElement>> scCandidates =  wNet.getKShortestServiceChainInIpLayer(2,originNode,destinationNode,vnfsToTraverse,Optional.empty(),Optional.empty());
//			System.out.println("Size = "+scCandidates.size());
//
//			scr.addServiceChain(scCandidates.iterator().next(),40);
		}

		return null;
	}

	@Override
	public String getDescription()
	{
		return null;
	}

	@Override
	public List<Triple<String, String, String>> getParameters()
	{
		return InputParameter.getInformationAllInputParameterFieldsOfObject(this);
	}
}
