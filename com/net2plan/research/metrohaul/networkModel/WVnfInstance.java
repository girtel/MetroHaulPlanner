package com.net2plan.research.metrohaul.networkModel;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import com.net2plan.interfaces.networkDesign.Resource;

public class WVnfInstance extends WAbstractNetworkElement
{
	public WVnfInstance(Resource r) { super (r); this.r = r; }

	final private Resource r;

	@Override
	public Resource getNe() { return r; }
	
	public WNode getHostingNode () { return new WNode (r.getHostNode()); }
	
	public String getType () { return r.getType(); }
	
	public String getName () { return r.getName(); }
	public void setName (String name) { r.setName(name); }
	
	public double getProcessingTimeInMs () { return r.getProcessingTimeToTraversingTrafficInMs(); }
	public void setProcessingTimeInMs (double procTimeInMs) { r.setProcessingTimeToTraversingTrafficInMs(procTimeInMs); }
	
	public SortedSet<WServiceChain> getTraversingServiceChains () { return r.getTraversingRoutes().stream().map(rr->new WServiceChain(rr)).collect(Collectors.toCollection(TreeSet::new)); }

	public SortedSet<WServiceChainRequest> getTraversingServiceChainRequests () { return r.getTraversingRoutes().stream().map(rr->new WServiceChainRequest(rr.getDemand())).collect(Collectors.toCollection(TreeSet::new)); }

	public double getOccupiedCapacityInGbps () { return r.getOccupiedCapacity(); }
	
	public double getOccupiedCapacityByTraversingRouteInGbps (WServiceChain sc) 
	{
		return r.getTraversingRouteOccupiedCapacity(sc.getNe());
	}

	public void setCapacity (double newProcessingCapacityInGbps , double newOccupiedCpu , double newOccupiedRam , double newOccupiedHd)
	{
		final Map<Resource,Double> map = new HashMap<> ();
		map.put (getHostingNode().getCpuBaseResource() , newOccupiedCpu);
		map.put (getHostingNode().getRamBaseResource() , newOccupiedRam);
		map.put (getHostingNode().getHdBaseResource() , newOccupiedHd);		
		r.setCapacity(newProcessingCapacityInGbps, map);
	}
	
	public double getCurrentCapacityInGbps () { return r.getCapacity(); } 
	
	public double getOccupiedCpus () 
	{ 
		return r.getCapacityOccupiedInBaseResource(getHostingNode().getCpuBaseResource());
	}
	public double getOccupiedHd () 
	{ 
		return r.getCapacityOccupiedInBaseResource(getHostingNode().getHdBaseResource());
	}
	public double getOccupiedRam () 
	{ 
		return r.getCapacityOccupiedInBaseResource(getHostingNode().getRamBaseResource());
	}
	
}
