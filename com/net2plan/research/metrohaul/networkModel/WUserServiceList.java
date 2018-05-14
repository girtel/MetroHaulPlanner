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
			if (row.size() != 9) throw new Net2PlanException ("Wrong format");
			final String userServiceUniqueId = row.get(0);
			if (res.containsKey(userServiceUniqueId)) throw new Net2PlanException ("User service names must be unique");
			final List<String> listVnfTypesToTraverseUpstream = Arrays.asList(StringUtils.split(row.get(1) ,WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER));
			final List<String> listVnfTypesToTraverseDownstream = Arrays.asList(StringUtils.split(row.get(2) ,WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER));
			final List<Double> sequenceTrafficExpansionFactorsRespectToBaseTrafficUpstream = Arrays.asList(row.get(3).split(WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER)).stream().map(s->Double.parseDouble(s)).collect(Collectors.toList());
			final List<Double> sequenceTrafficExpansionFactorsRespectToBaseTrafficDownstream = Arrays.asList(row.get(4).split(WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER)).stream().map(s->Double.parseDouble(s)).collect(Collectors.toList());
			final double maxLatencyFromServingMetroNodeToFirstVnf_ms = Double.parseDouble(row.get(5));
			final double injectionDownstreamExpansionFactorRespecToBaseTrafficUpstream = Double.parseDouble(row.get(6));
			final boolean isEndingInCoreNode = Boolean.parseBoolean(row.get(7));
			final String arbitraryParamString = row.get(8);
			res.put(userServiceUniqueId, new WUserService(userServiceUniqueId, listVnfTypesToTraverseUpstream, listVnfTypesToTraverseDownstream, sequenceTrafficExpansionFactorsRespectToBaseTrafficUpstream, sequenceTrafficExpansionFactorsRespectToBaseTrafficDownstream, maxLatencyFromServingMetroNodeToFirstVnf_ms, injectionDownstreamExpansionFactorRespecToBaseTrafficUpstream, isEndingInCoreNode , arbitraryParamString));
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
			infoThisVnf.add(entry.getValue().getListVnfTypesToTraverseUpstream().stream().collect(Collectors.joining(WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER)));
			infoThisVnf.add(entry.getValue().getListVnfTypesToTraverseDownstream().stream().collect(Collectors.joining(WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER)));
			infoThisVnf.add(entry.getValue().getSequenceTrafficExpansionFactorsRespectToBaseTrafficUpstream().stream().map(n->n.toString()).collect(Collectors.joining(WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER)) + "");
			infoThisVnf.add(entry.getValue().getSequenceTrafficExpansionFactorsRespectToBaseTrafficDownstream().stream().map(n->n.toString()).collect(Collectors.joining(WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER)) + "");
			infoThisVnf.add(entry.getValue().getMaxLatencyFromServingMetroNodeToFirstVnf_ms() + "");
			infoThisVnf.add(entry.getValue().getInjectionDownstreamExpansionFactorRespecToBaseTrafficUpstream() + "");
			infoThisVnf.add(new Boolean (entry.getValue().isEndingInCoreNode()).toString());
			matrix.add(infoThisVnf);
		}
		np.setAttributeAsStringMatrix(ATTNAME_USERSERVICELIST, matrix);
	}
	
	@Override
	public NetPlan getNe() { return np; }	
	
	public SortedSet<String> getUserServiceNames () { return new TreeSet<> (getUserServicesInfo().keySet()); }
	
}
