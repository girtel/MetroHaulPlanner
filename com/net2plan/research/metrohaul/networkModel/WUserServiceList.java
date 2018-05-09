package com.net2plan.research.metrohaul.networkModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.utils.Pair;
import com.net2plan.utils.Quadruple;
import com.net2plan.utils.Triple;

public class WUserServiceList extends WAbstractNetworkElement
{
	private static final String ATTNAME_USERSERVICELIST = "userServiceListMatrix";
	private final NetPlan np;
	public WUserServiceList (NetPlan np) { super (np); this.np = np; }
	
	public SortedMap<String , WUserService> getVnfInfo ()
	{
		final SortedMap<String , WUserService> res = new TreeMap<> ();
		final List<List<String>> matrix = getNe().getAttributeAsStringMatrix(ATTNAME_USERSERVICELIST, null);
		if (matrix == null) throw new Net2PlanException("Wrong format");
		for (List<String> row : matrix)
		{
			if (row.size() != 7) throw new Net2PlanException ("Wrong format");
			final String userServiceUniqueId = row.get(0);
			if (res.containsKey(userServiceUniqueId)) throw new Net2PlanException ("User service names must be unique");
			final double listVnfTypesToTraverse = Double.parseDouble(row.get(1));
			final List<Double> sequenceOfUpstreamTrafficExpansionFactorsRespectToBaseUserPerVnf = Arrays.asList(row.get(2).split(" ")).stream().map(s->Double.parseDouble(s)).collect(Collectors.toList());
			final double averageBaseBandwidthPerUser_Mbps = Double.parseDouble(row.get(3));
			final double maxLatencyFromServingMetroNodeToFirstVnf_ms = Double.parseDouble(row.get(4));
			final boolean isBidirectional = Boolean.parseBoolean(row.get(5));
			final List<Double> sequenceOfDownstreamTrafficExpansionFactorsRespectToBaseUserPerVnf = Arrays.asList(row.get(6).split(" ")).stream().map(s->Double.parseDouble(s)).collect(Collectors.toList());
			res.put(userServiceUniqueId, new WUserService(userServiceUniqueId,
					listVnfTypesToTraverse,
					sequenceOfUpstreamTrafficExpansionFactorsRespectToBaseUserPerVnf ,
					averageBaseBandwidthPerUser_Mbps , 
					maxLatencyFromServingMetroNodeToFirstVnf_ms , 
					isBidirectional , 
					sequenceOfDownstreamTrafficExpansionFactorsRespectToBaseUserPerVnf));

//			public WUserService(String userServiceUniqueId, List<String> listVnfTypesToTraverse,
//					List<Double> sequenceOfUpstreamTrafficExpansionFactorsRespectToBaseUserPerVnf,
//					double averageBaseBandwidthPerUser_Mbps, double maxLatencyFromServingMetroNodeToFirstVnf_ms,
//					boolean isBidirectional, List<Double> sequenceOfDownstreamTrafficExpansionFactorsRespectToBaseUserPerVnf)

		}
		return res;
	}

	public void addOrUpdateVnfInfo (String vnfTypeName , WVnfType info)
	{
		final SortedMap<String , WVnfType> newInfo = this.getVnfInfo();
		newInfo.put(vnfTypeName, info);
		this.setVnfInfo(newInfo);
	}
	
	public void removeVnfInfo (String vnfTypeName)
	{
		final SortedMap<String , WVnfType> newInfo = this.getVnfInfo();
		newInfo.remove(vnfTypeName);
		this.setVnfInfo(newInfo);
	}

	public void setVnfInfo (SortedMap<String , WVnfType> newInfo)
	{
		final List<List<String>> matrix = new ArrayList<> ();
		for (Entry<String , WVnfType> entry : newInfo.entrySet())
		{
			final List<String> infoThisVnf = new LinkedList<> ();
			infoThisVnf.add(entry.getKey());
			infoThisVnf.add(entry.getValue().getMaxInputTrafficPerVnfInstance_Gbps() + "");
			infoThisVnf.add(entry.getValue().getOccupCpu() + "");
			infoThisVnf.add(entry.getValue().getOccupRam() + "");
			infoThisVnf.add(entry.getValue().getOccupHd() + "");
			infoThisVnf.add(entry.getValue().isConstrained()? "1"  : "0");
			infoThisVnf.add(entry.getValue().getValidMetroNodesForInstantiation().stream().map(n->n.getName()).collect(Collectors.joining(" ")));
			matrix.add(infoThisVnf);
		}
		np.setAttributeAsStringMatrix(ATTNAME_USERSERVICELIST, matrix);
	}
	
	@Override
	public NetPlan getNe() { return np; }	
	
	public SortedSet<String> getVnfTypeNames () { return new TreeSet<> (getVnfInfo().keySet()); }
	
}
