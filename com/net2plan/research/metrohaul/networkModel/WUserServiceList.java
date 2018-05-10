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
import com.net2plan.utils.StringUtils;

public class WUserServiceList extends WAbstractNetworkElement
{
	private static final String ATTNAME_USERSERVICELIST = "userServiceListMatrix";
	private final NetPlan np;
	public WUserServiceList (NetPlan np) { super (np); this.np = np; }
	
	public SortedMap<String , WUserService> getUserServicesInfo ()
	{
		final SortedMap<String , WUserService> res = new TreeMap<> ();
		final List<List<String>> matrix = getNe().getAttributeAsStringMatrix(ATTNAME_USERSERVICELIST, null);
		if (matrix == null) throw new Net2PlanException("Wrong format");
		for (List<String> row : matrix)
		{
			if (row.size() != 8) throw new Net2PlanException ("Wrong format");
			final String userServiceUniqueId = row.get(0);
			if (res.containsKey(userServiceUniqueId)) throw new Net2PlanException ("User service names must be unique");
			final List<String> listVnfTypesToTraverse = Arrays.asList(StringUtils.split(row.get(1) ,WNetConstants.WNODE_NODENAMEINVALIDCHARACTER));
			final List<Double> sequenceOfUpstreamTrafficExpansionFactorsRespectToBaseUserPerVnf = Arrays.asList(row.get(2).split(WNetConstants.WNODE_NODENAMEINVALIDCHARACTER)).stream().map(s->Double.parseDouble(s)).collect(Collectors.toList());
			final double averageBaseBandwidthPerUser_Mbps = Double.parseDouble(row.get(3));
			final double maxLatencyFromServingMetroNodeToFirstVnf_ms = Double.parseDouble(row.get(4));
			final boolean isBidirectional = Boolean.parseBoolean(row.get(5));
			final boolean isEndingInCoreNode = Boolean.parseBoolean(row.get(6));
			final List<Double> sequenceOfDownstreamTrafficExpansionFactorsRespectToBaseUserPerVnf = Arrays.asList(row.get(7).split(WNetConstants.WNODE_NODENAMEINVALIDCHARACTER)).stream().map(s->Double.parseDouble(s)).collect(Collectors.toList());
			res.put(userServiceUniqueId, new WUserService(userServiceUniqueId,
					listVnfTypesToTraverse,
					sequenceOfUpstreamTrafficExpansionFactorsRespectToBaseUserPerVnf ,
					averageBaseBandwidthPerUser_Mbps , 
					maxLatencyFromServingMetroNodeToFirstVnf_ms , 
					isBidirectional , 
					isEndingInCoreNode , 
					sequenceOfDownstreamTrafficExpansionFactorsRespectToBaseUserPerVnf));
		}
		return res;
	}

	public void addOrUpdateVnfInfo (String userServiceName , WUserService info)
	{
		final SortedMap<String , WUserService> newInfo = this.getUserServicesInfo();
		newInfo.put(userServiceName, info);
		this.setUserServicesInfo(newInfo);
	}
	
	public void removeVnfInfo (String userServiceName)
	{
		final SortedMap<String , WUserService> newInfo = this.getUserServicesInfo();
		newInfo.remove(userServiceName);
		this.setUserServicesInfo(newInfo);
	}

	public void setUserServicesInfo (SortedMap<String , WUserService> newInfo)
	{
		final List<List<String>> matrix = new ArrayList<> ();
		for (Entry<String , WUserService> entry : newInfo.entrySet())
		{
			final List<String> infoThisVnf = new LinkedList<> ();
			infoThisVnf.add(entry.getKey());
			infoThisVnf.add(entry.getValue().getListVnfTypesToTraverse().stream().map(n->n.toString()).collect(Collectors.joining(WNetConstants.WNODE_NODENAMEINVALIDCHARACTER)));
			infoThisVnf.add(entry.getValue().getSequenceOfUpstreamTrafficExpansionFactorsRespectToBaseUserPerVnf().stream().map(n->n.toString()).collect(Collectors.joining(WNetConstants.WNODE_NODENAMEINVALIDCHARACTER)) + "");
			infoThisVnf.add(entry.getValue().getAverageBaseBandwidthPerUser_Mbps() + "");
			infoThisVnf.add(entry.getValue().getMaxLatencyFromServingMetroNodeToFirstVnf_ms() + "");
			infoThisVnf.add(new Boolean (entry.getValue().isBidirectional()).toString());
			infoThisVnf.add(new Boolean (entry.getValue().isEndingInCoreNode()).toString());
			infoThisVnf.add(entry.getValue().getSequenceOfDownstreamTrafficExpansionFactorsRespectToBaseUserPerVnf().stream().map(n->n.toString()).collect(Collectors.joining(WNetConstants.WNODE_NODENAMEINVALIDCHARACTER)));
			matrix.add(infoThisVnf);
		}
		np.setAttributeAsStringMatrix(ATTNAME_USERSERVICELIST, matrix);
	}
	
	@Override
	public NetPlan getNe() { return np; }	
	
	public SortedSet<String> getUserServiceNames () { return new TreeSet<> (getUserServicesInfo().keySet()); }
	
}
