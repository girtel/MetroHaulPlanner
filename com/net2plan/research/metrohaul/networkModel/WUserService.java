package com.net2plan.research.metrohaul.networkModel;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.net2plan.utils.Quadruple;
import com.net2plan.utils.Triple;

public class WUserService
{
	final private String userServiceUniqueId;
	final private List<String> listVnfTypesToTraverse;
	final private List<Double> sequenceOfUpstreamTrafficExpansionFactorsRespectToBaseUserPerVnf;
	final double averageBaseBandwidthPerUser_Mbps;
	final double maxLatencyFromServingMetroNodeToFirstVnf_ms;
	final boolean isBidirectional;
	final List<Double> sequenceOfDownstreamTrafficExpansionFactorsRespectToBaseUserPerVnf;
	
	public WUserService(String userServiceUniqueId, List<String> listVnfTypesToTraverse,
			List<Double> sequenceOfUpstreamTrafficExpansionFactorsRespectToBaseUserPerVnf,
			double averageBaseBandwidthPerUser_Mbps, double maxLatencyFromServingMetroNodeToFirstVnf_ms,
			boolean isBidirectional, List<Double> sequenceOfDownstreamTrafficExpansionFactorsRespectToBaseUserPerVnf)
	{
		super();
		this.userServiceUniqueId = userServiceUniqueId;
		this.listVnfTypesToTraverse = listVnfTypesToTraverse;
		this.sequenceOfUpstreamTrafficExpansionFactorsRespectToBaseUserPerVnf = sequenceOfUpstreamTrafficExpansionFactorsRespectToBaseUserPerVnf;
		this.averageBaseBandwidthPerUser_Mbps = averageBaseBandwidthPerUser_Mbps;
		this.maxLatencyFromServingMetroNodeToFirstVnf_ms = maxLatencyFromServingMetroNodeToFirstVnf_ms;
		this.isBidirectional = isBidirectional;
		this.sequenceOfDownstreamTrafficExpansionFactorsRespectToBaseUserPerVnf = sequenceOfDownstreamTrafficExpansionFactorsRespectToBaseUserPerVnf;
	}
	public String getUserServiceUniqueId()
	{
		return userServiceUniqueId;
	}
	public List<String> getListVnfTypesToTraverse()
	{
		return listVnfTypesToTraverse;
	}
	public List<Double> getSequenceOfUpstreamTrafficExpansionFactorsRespectToBaseUserPerVnf()
	{
		return sequenceOfUpstreamTrafficExpansionFactorsRespectToBaseUserPerVnf;
	}
	public double getAverageBaseBandwidthPerUser_Mbps()
	{
		return averageBaseBandwidthPerUser_Mbps;
	}
	public double getMaxLatencyFromServingMetroNodeToFirstVnf_ms()
	{
		return maxLatencyFromServingMetroNodeToFirstVnf_ms;
	}
	public boolean isBidirectional()
	{
		return isBidirectional;
	}
	public List<Double> getSequenceOfDownstreamTrafficExpansionFactorsRespectToBaseUserPerVnf()
	{
		return sequenceOfDownstreamTrafficExpansionFactorsRespectToBaseUserPerVnf;
	}

	
}
