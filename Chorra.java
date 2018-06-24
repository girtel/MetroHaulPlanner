import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.research.metrohaul.networkModel.WFiber;
import com.net2plan.research.metrohaul.networkModel.WLightpathRequest;
import com.net2plan.research.metrohaul.networkModel.WNet;
import com.net2plan.research.metrohaul.networkModel.WNode;
import com.net2plan.utils.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Chorra implements IAlgorithm
{

	@Override
	public String executeAlgorithm(NetPlan netPlan, Map<String, String> algorithmParameters, Map<String, String> net2planParameters)
	{
		//Asumming empty Net2Plan!
		netPlan.addNode(0, 0, "Test1", null);
		netPlan.addNode(0, 0, "Test2", null);
		netPlan.addLayer("ipLayer", "", "Gbps", "Gbps", null, null);

		WNet wNet = new WNet(netPlan);
		/* Adding some nodes */
		WNode node1 = wNet.addNode(1, 0.0, "Node1", "");
		WNode node2 = wNet.addNode(1, 1, "Node2", "");
		WNode node3 = wNet.addNode(2.0, 1.0, "Node3", "");
		WNode node4 = wNet.addNode(0.0, 1.0, "Node4", "");

		//Adding some links between the nodes
		List<Integer> validOpticalSlotRanges = new ArrayList<>();
		for(int i = 0; i < 100; i++) validOpticalSlotRanges.add(i);

		wNet.addFiber(node1, node2, validOpticalSlotRanges, 20, true);
		wNet.addFiber(node1, node3, validOpticalSlotRanges, 20, true);
		wNet.addFiber(node1, node4, validOpticalSlotRanges, 20, true);

		wNet.addFiber(node2, node3, validOpticalSlotRanges, 20, true);
		wNet.addFiber(node3, node4, validOpticalSlotRanges, 20, true);

		//Adding some lightpaths
		WLightpathRequest lprequest1 = wNet.addLightpathRequest(node4, node3, 50, false);
		List<List<WFiber>> paths = wNet.getKShortestWdmPath(1,node4,node3,null);
//		lprequest1.addLightpathUnregenerated(paths.get(0),);


		return "Oki :)";
	}

	@Override
	public String getDescription()
	{
		return "Some testing";
	}

	@Override
	public List<Triple<String, String, String>> getParameters()
	{
		return null;
	}
}
