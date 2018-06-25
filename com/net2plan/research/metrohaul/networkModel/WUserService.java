package com.net2plan.research.metrohaul.networkModel;

import java.util.Arrays;
import java.util.List;

import com.net2plan.interfaces.networkDesign.Net2PlanException;

public class WUserService
{
	final private String userServiceUniqueId;
	final private List<String> listVnfTypesToTraverseUpstream;
	final private List<String> listVnfTypesToTraverseDownstream;
	final private List<Double> sequenceTrafficExpansionFactorsRespectToBaseTrafficUpstream;
	final private List<Double> sequenceTrafficExpansionFactorsRespectToBaseTrafficDownstream;
	final private List<Double> listMaxLatencyFromInitialToVnfStart_ms_upstream; 
	final private List<Double> listMaxLatencyFromInitialToVnfStart_ms_downstream; 
	final private double injectionDownstreamExpansionFactorRespecToBaseTrafficUpstream;
	final private boolean isEndingInCoreNode;
	private String arbitraryParamString;
	public WUserService(String userServiceUniqueId, List<String> listVnfTypesToTraverseUpstream,
			List<String> listVnfTypesToTraverseDownstream,
			List<Double> sequenceTrafficExpansionFactorsRespectToBaseTrafficUpstream,
			List<Double> sequenceTrafficExpansionFactorsRespectToBaseTrafficDownstream,
			List<Double> listMaxLatencyFromInitialToVnfStart_ms_upstream,
			List<Double> listMaxLatencyFromInitialToVnfStart_ms_downstream,
			double injectionDownstreamExpansionFactorRespecToBaseTrafficUpstream, boolean isEndingInCoreNode,
			String arbitraryParamString)
	{
		super();
		this.userServiceUniqueId = userServiceUniqueId;
		this.listVnfTypesToTraverseUpstream = listVnfTypesToTraverseUpstream;
		this.listVnfTypesToTraverseDownstream = listVnfTypesToTraverseDownstream;
		this.sequenceTrafficExpansionFactorsRespectToBaseTrafficUpstream = sequenceTrafficExpansionFactorsRespectToBaseTrafficUpstream;
		this.sequenceTrafficExpansionFactorsRespectToBaseTrafficDownstream = sequenceTrafficExpansionFactorsRespectToBaseTrafficDownstream;
		this.listMaxLatencyFromInitialToVnfStart_ms_upstream = listMaxLatencyFromInitialToVnfStart_ms_upstream;
		this.listMaxLatencyFromInitialToVnfStart_ms_downstream = listMaxLatencyFromInitialToVnfStart_ms_downstream;
		this.injectionDownstreamExpansionFactorRespecToBaseTrafficUpstream = injectionDownstreamExpansionFactorRespecToBaseTrafficUpstream;
		this.isEndingInCoreNode = isEndingInCoreNode;
		this.arbitraryParamString = arbitraryParamString;
	}
	public String getArbitraryParamString()
	{
		return arbitraryParamString;
	}
	public void setArbitraryParamString(String arbitraryParamString)
	{
		this.arbitraryParamString = arbitraryParamString;
	}
	public String getUserServiceUniqueId()
	{
		return userServiceUniqueId;
	}
	public List<String> getListVnfTypesToTraverseUpstream()
	{
		return listVnfTypesToTraverseUpstream;
	}
	public List<String> getListVnfTypesToTraverseDownstream()
	{
		return listVnfTypesToTraverseDownstream;
	}
	public List<Double> getSequenceTrafficExpansionFactorsRespectToBaseTrafficUpstream()
	{
		return sequenceTrafficExpansionFactorsRespectToBaseTrafficUpstream;
	}
	public List<Double> getSequenceTrafficExpansionFactorsRespectToBaseTrafficDownstream()
	{
		return sequenceTrafficExpansionFactorsRespectToBaseTrafficDownstream;
	}
	public List<Double> getListMaxLatencyFromInitialToVnfStart_ms_upstream()
	{
		return listMaxLatencyFromInitialToVnfStart_ms_upstream;
	}
	public List<Double> getListMaxLatencyFromInitialToVnfStart_ms_downstream()
	{
		return listMaxLatencyFromInitialToVnfStart_ms_downstream;
	}
	public double getInjectionDownstreamExpansionFactorRespecToBaseTrafficUpstream()
	{
		return injectionDownstreamExpansionFactorRespecToBaseTrafficUpstream;
	}
	public boolean isEndingInCoreNode()
	{
		return isEndingInCoreNode;
	}
	
	
}
