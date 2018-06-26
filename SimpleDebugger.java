import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.research.metrohaul.networkModel.*;
import com.net2plan.utils.Pair;
import com.net2plan.utils.Triple;

import java.util.*;

public class SimpleDebugger implements IAlgorithm
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
		List<Integer> validOpticalSlotRanges = new LinkedList<>();
		validOpticalSlotRanges.add(0);
		validOpticalSlotRanges.add(100);

		wNet.addFiber(node1, node2, validOpticalSlotRanges, 20, true);
		wNet.addFiber(node1, node3, validOpticalSlotRanges, 20, true);
		wNet.addFiber(node1, node4, validOpticalSlotRanges, 20, true);

		Pair<WFiber, WFiber> fiber1 = wNet.addFiber(node2, node3, validOpticalSlotRanges, 20, true);
		wNet.addFiber(node3, node4, validOpticalSlotRanges, 20, true);

		//Adding some lightpaths
		WLightpathRequest lprequest1 = wNet.addLightpathRequest(node4, node3, 50, false);
		List<List<WFiber>> paths = wNet.getKShortestWdmPath(1, node4, node3, Optional.empty());

		OpticalSpectrumManager osm = OpticalSpectrumManager.createFromRegularLps(wNet);
		Optional<Integer> slots = osm.spectrumAssignment_firstFit(paths.get(0), 1, Optional.empty());
		if(! slots.isPresent()) throw new Net2PlanException("WTF");

		SortedSet<Integer> sortedSlots = new TreeSet<>();
		sortedSlots.add(slots.get());
		boolean isAllocatable = osm.isAllocatable(paths.get(0), new TreeSet<>(sortedSlots));
		System.out.println("Is allocatable = " + isAllocatable);

		WLightpathUnregenerated lp1 = lprequest1.addLightpathUnregenerated(paths.get(0), sortedSlots, null, null, false);
		osm.allocateOccupation(lp1, paths.get(0), new TreeSet<>(sortedSlots));
		slots = osm.spectrumAssignment_firstFit(paths.get(0), 1, Optional.empty());
		System.out.println("Slots = " + slots.get().intValue());

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
