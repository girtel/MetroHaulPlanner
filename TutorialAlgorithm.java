import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.research.metrohaul.networkModel.*;
import com.net2plan.utils.InputParameter;
import com.net2plan.utils.Triple;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TutorialAlgorithm implements IAlgorithm
{
	private InputParameter linerate_Gbps            = new InputParameter("linerate_Gbps", 40.0, "Linerate (in Gbps) per ligthpath", 1.0, true, 1000.0, true);
	private InputParameter slotsPerLightpath        = new InputParameter("slotsPerLightpath", 4, "Number of occupied slots per lightpath", 1, Integer.MAX_VALUE);
	private InputParameter K                        = new InputParameter("K", 3, "Number of candidate shortest paths to compute per LP/SC", 1, 100);
	private InputParameter trafficIntensityTimeSlot = new InputParameter("trafficIntensityTimeSlot", "#select# Morning Afternoon Evening", "Traffic intensity per time slot (as defined in the design/spreadsheet");

	public static void main(String[] args)
	{
		NetPlan netPlan = NetPlan.loadFromFile(new File("ExcelTest_v2.n2p"));
		Map<String, String> parameters = new HashMap<>();
		parameters.put("linerate_Gbps", String.valueOf(40.0));
		parameters.put("slotsPerLightpath", String.valueOf(4));
		parameters.put("K", String.valueOf(3));
		parameters.put("trafficIntensityTimeSlot", "Morning");

		TutorialAlgorithm tutorialAlgorithm = new TutorialAlgorithm();
		tutorialAlgorithm.getParameters();
		System.out.println(tutorialAlgorithm.executeAlgorithm(netPlan,parameters,null));
	}

	@Override
	public String executeAlgorithm(NetPlan netPlan, Map<String, String> algorithmParameters, Map<String, String> net2planParameters)
	{
		//First of all, initialize all parameters
		InputParameter.initializeAllInputParameterFieldsOfObject(this, algorithmParameters);

		final WNet wNet = new WNet(netPlan);
		final OpticalSpectrumManager osm = OpticalSpectrumManager.createFromRegularLps(wNet);
		//Perform here initial checks

		/*
			Add lightpaths
		 */
		for(WNode node1 : wNet.getNodes())
		{
			for(WNode node2 : wNet.getNodes())
			{
				if(node1.getName().equals(node2.getName())) continue;
				List<List<WFiber>> paths = wNet.getKShortestWdmPath(K.getInt(), node1, node2, Optional.empty());
				int initialSlot = - 1;
				List<WFiber> selectedPath = null;
				for(List<WFiber> path : paths)
				{
					Optional<Integer> slot = osm.spectrumAssignment_firstFit(path, slotsPerLightpath.getInt(), Optional.empty());
					if(slot.isPresent())
					{
						initialSlot = slot.get();
						selectedPath = path;
						break;
					}
				}
				if(initialSlot == - 1) throw new Net2PlanException("No wavelengths found to allocate a lightpath between " + node1.getName() + " and " + node2.getName());
				WLightpathRequest lpr = wNet.addLightpathRequest(node1, node2, linerate_Gbps.getDouble(), false);
				SortedSet<Integer> slotsIds = IntStream.range(initialSlot, initialSlot + slotsPerLightpath.getInt()).boxed().collect(Collectors.toCollection(TreeSet::new));
				WLightpathUnregenerated lp = lpr.addLightpathUnregenerated(selectedPath, slotsIds, Optional.empty(), Optional.empty(), false);
				osm.allocateOccupation(lp, selectedPath, slotsIds);

				// Create IP link and couple it with the LP
				WIpLink ipLink = wNet.addIpLink(node1, node2, linerate_Gbps.getDouble(), false);
				lpr.coupleToIpLink(ipLink);
			}

			/*
				Deploy service chain
			 */
			for(WServiceChainRequest serviceChainRequest : wNet.getServiceChainRequests())
			{
				Optional<Double> ti = serviceChainRequest.getTrafficIntensityInfo(trafficIntensityTimeSlot.getString());
				if(! ti.isPresent())
				{
					System.out.println("No traffic intensity defined (" + trafficIntensityTimeSlot.getInt() + ") for SC " + serviceChainRequest.getId());
					continue;
				}
				final double trafficIntensity = ti.get();
				final SortedSet<WNode> potentialOrigins = serviceChainRequest.getPotentiallyValidOrigins();
				final SortedSet<WNode> potentialDestinations = serviceChainRequest.getPotentiallyValidDestinations();
				final List<String> vnfToTraverse = serviceChainRequest.getSequenceVnfTypes();

				boolean isSCAllocated = false;
				for(WNode origin : potentialOrigins)
				{
					for(WNode destination : potentialDestinations)
					{
						if(origin.getName().equals(destination.getName())) continue;
						List<List<? extends WAbstractNetworkElement>> paths = wNet.getKShortestServiceChainInIpLayer(K.getInt(), origin, destination, vnfToTraverse, Optional.empty(), Optional.empty());
						if(paths.isEmpty())
							continue;
						WServiceChain serviceChain = serviceChainRequest.addServiceChain(paths.get(0), trafficIntensity);
						isSCAllocated = true;
						if(isSCAllocated) break;
					}
					if(isSCAllocated) break;
				}

			}
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
