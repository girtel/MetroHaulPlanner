package com.net2plan.research.metrohaul.networkModel;

import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.Net2PlanException;

public class WIpLink extends WAbstractNetworkElement
{
	private static final String ATTNAMECOMMONPREFIX = "IpLink_";
	private static final String ATTNAMESUFFIX_NOMINALCAPACITYGBPS = "nominalCapacityGbps";

	final private Link npLink;
	
	public Link getNe () { return (Link) e; }
	public WIpLink (Link e) { super (e); this.npLink = e; }

	public boolean isBidirectional () { return npLink.isBidirectional(); }

	public WIpLink getBidirectionalPair () { if (!this.isBidirectional()) throw new Net2PlanException ("Not a bidirectional link"); return new WIpLink (npLink.getBidirectionalPair()); }

	public double getLengthIfNotCoupledInKm () { return npLink.getLengthInKm(); } 

	public void setLengthIfNotCoupledInKm (double lengthInKm) { npLink.setLengthInKm(lengthInKm); } 
	
	public double getNominalCapacityGbps () { final Double res = npLink.getAttributeAsDouble(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_NOMINALCAPACITYGBPS, null); if (res == null) throw new RuntimeException (); return res; } 
	
	public double getCurrentCapacityGbps () { return npLink.getCapacity(); } 
	
	public double getCarriedTrafficGbps () { return npLink.getCarriedTraffic(); }

	public void coupleToLightpathRequest (WLightpathRequest lpReq) 
	{ 
		if (this.getNominalCapacityGbps() != lpReq.getLineRateGbps()) throw new Net2PlanException ("Cannot couple an IP link with a different line rate of the lightpath request");
		npLink.coupleToLowerLayerDemand(lpReq.getNe()); 
	}
	
	public void decouple () { if (!npLink.isCoupled()) return; npLink.getCoupledDemand().decouple(); }
	
	public boolean isCoupledtoLpRequest () { return npLink.isCoupled(); } 
	
	public WLightpathRequest getCoupledLpRequest () { if (!isCoupledtoLpRequest()) throw new Net2PlanException ("Not coupled"); return new WLightpathRequest(npLink.getCoupledDemand()); }

	public double getWorstCaseLengthInKm ()
	{
		if (this.isCoupledtoLpRequest()) return getCoupledLpRequest().getWorstCaseLengthInKm();
		return getLengthIfNotCoupledInKm();
	}

	public double getWorstCasePropagationDelayInMs ()
	{
		if (this.isCoupledtoLpRequest()) return getCoupledLpRequest().getWorstCasePropagationDelayMs();
		return npLink.getPropagationDelayInMs();
	}

	
}
