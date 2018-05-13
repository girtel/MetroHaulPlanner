package com.net2plan.research.metrohaul.networkModel;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.net2plan.utils.Quadruple;
import com.net2plan.utils.Triple;

public class WUserService
{
	final private String userServiceUniqueId;
	final private List<String> listVnfTypesToTraverseUpstream;
	final private List<String> listVnfTypesToTraverseDownstream;
	final private List<Double> sequenceTrafficExpansionFactorsRespectToBaseTrafficUpstream;
	final private List<Double> sequenceTrafficExpansionFactorsRespectToBaseTrafficDownstream;
	final double maxLatencyFromServingMetroNodeToFirstVnf_ms;
	final double injectionDownstreamExpansionFactorRespecToBaseTrafficUpstream;
	final boolean isEndingInCoreNode;
	
	public WUserService(String userServiceUniqueId, List<String> listVnfTypesToTraverseUpstream,
			List<String> listVnfTypesToTraverseDownstream,
			List<Double> sequenceTrafficExpansionFactorsRespectToBaseTrafficUpstream,
			List<Double> sequenceTrafficExpansionFactorsRespectToBaseTrafficDownstream,
			double maxLatencyFromServingMetroNodeToFirstVnf_ms,
			double injectionDownstreamExpansionFactorRespecToBaseTrafficUpstream, boolean isEndingInCoreNode) {
		super();
		this.userServiceUniqueId = userServiceUniqueId;
		this.listVnfTypesToTraverseUpstream = listVnfTypesToTraverseUpstream;
		this.listVnfTypesToTraverseDownstream = listVnfTypesToTraverseDownstream;
		this.sequenceTrafficExpansionFactorsRespectToBaseTrafficUpstream = sequenceTrafficExpansionFactorsRespectToBaseTrafficUpstream;
		this.sequenceTrafficExpansionFactorsRespectToBaseTrafficDownstream = sequenceTrafficExpansionFactorsRespectToBaseTrafficDownstream;
		this.maxLatencyFromServingMetroNodeToFirstVnf_ms = maxLatencyFromServingMetroNodeToFirstVnf_ms;
		this.injectionDownstreamExpansionFactorRespecToBaseTrafficUpstream = injectionDownstreamExpansionFactorRespecToBaseTrafficUpstream;
		this.isEndingInCoreNode = isEndingInCoreNode;
	}
	public String getUserServiceUniqueId() {
		return userServiceUniqueId;
	}
	public List<String> getListVnfTypesToTraverseUpstream() {
		return listVnfTypesToTraverseUpstream;
	}
	public List<String> getListVnfTypesToTraverseDownstream() {
		return listVnfTypesToTraverseDownstream;
	}
	public List<Double> getSequenceTrafficExpansionFactorsRespectToBaseTrafficUpstream() {
		return sequenceTrafficExpansionFactorsRespectToBaseTrafficUpstream;
	}
	public List<Double> getSequenceTrafficExpansionFactorsRespectToBaseTrafficDownstream() {
		return sequenceTrafficExpansionFactorsRespectToBaseTrafficDownstream;
	}
	public double getMaxLatencyFromServingMetroNodeToFirstVnf_ms() {
		return maxLatencyFromServingMetroNodeToFirstVnf_ms;
	}
	public double getInjectionDownstreamExpansionFactorRespecToBaseTrafficUpstream() {
		return injectionDownstreamExpansionFactorRespecToBaseTrafficUpstream;
	}
	public boolean isEndingInCoreNode() {
		return isEndingInCoreNode;
	}
	
}
