package com.net2plan.research.metrohaul.networkModel;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.interfaces.networkDesign.Resource;

public class WNode extends WAbstractNetworkElement
{
	private static final String ATTNAMECOMMONPREFIX = "Node_";
	private static final String ATTNAMESUFFIX_TYPE = "type";
	private static final String ATTNAMESUFFIX_ISCONNECTEDTOCORE = "isConnectedToNetworkCore";
	private static final String RESOURCETYPE_CPU = "CPU";
	private static final String RESOURCETYPE_RAM = "RAM";
	private static final String RESOURCETYPE_HD = "HD";
	private final Node n;
	
	Resource getCpuBaseResource ()
	{
		final Set<Resource> cpuResources = n.getResources(RESOURCETYPE_CPU);
		assert cpuResources.size() < 2;
		if (cpuResources.isEmpty()) setTotalNumCpus(0);
		assert cpuResources.size() == 1;
		return n.getResources(RESOURCETYPE_CPU).iterator().next();
	}
	Resource getRamBaseResource ()
	{
		final Set<Resource> ramResources = n.getResources(RESOURCETYPE_RAM);
		assert ramResources.size() < 2;
		if (ramResources.isEmpty()) setTotalRamGB(0);
		assert ramResources.size() == 1;
		return n.getResources(RESOURCETYPE_RAM).iterator().next();
	}
	Resource getHdBaseResource ()
	{
		final Set<Resource> hdResources = n.getResources(RESOURCETYPE_HD);
		assert hdResources.size() < 2;
		if (hdResources.isEmpty()) setTotalHdGB(0);
		assert hdResources.size() == 1;
		return n.getResources(RESOURCETYPE_HD).iterator().next();
	}
	
	public boolean isVirtualNode () { return n.getIndex() <= 2; }
	public Node getNe () { return (Node) e; }
	public WNode (Node n) { super (n); this.n = n; }

	public String getName () { return n.getName(); }
	public void setName (String name) { if (name.contains(" ")) throw new Net2PlanException("Names cannot contain spaces");  n.setName(name); }
	public Point2D getNodePositionXY () { return n.getXYPositionMap(); }
	public void setNodePositionXY (Point2D position) { n.setXYPositionMap(position); }
	public String getType () { return getAttributeOrDefault(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_TYPE , ""); }
	public void setType (String type) { n.setAttribute(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_TYPE , type); }
	public boolean isConnectedToNetworkCore () { return getAttributeAsBooleanOrDefault(ATTNAMECOMMONPREFIX +ATTNAMESUFFIX_ISCONNECTEDTOCORE , WNetConstants.WNODE_DEFAULT_ISCONNECTEDTOCORE); }
	public void setIsConnectedToNetworkCore (boolean isConnectedToCore) { n.setAttribute(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_ISCONNECTEDTOCORE , new Boolean (isConnectedToCore).toString()); }
	public double getPopulation () { return n.getPopulation(); }
	public void setPoputlation (double population) { n.setPopulation(population); }
	public double getTotalNumCpus () { return n.getResources(RESOURCETYPE_CPU).stream().mapToDouble(r->r.getCapacity()).sum (); }
	public void setTotalNumCpus (double totalNumCpus) 
	{ 
		final Set<Resource> res = n.getResources(RESOURCETYPE_CPU);
		if (res.size() > 1) throw new Net2PlanException ("Format error");
		if (res.isEmpty()) 
			res.add(n.getNetPlan().addResource(RESOURCETYPE_CPU, RESOURCETYPE_CPU, n, totalNumCpus, RESOURCETYPE_CPU, new HashMap<> (), 0.0, null));
		else 
			res.iterator().next().setCapacity(totalNumCpus, new HashMap<> ());
	}
	public double getTotalRamGB () { return n.getResources(RESOURCETYPE_RAM).stream().mapToDouble(r->r.getCapacity()).sum (); }
	public void setTotalRamGB (double totalRamGB) 
	{ 
		final Set<Resource> res = n.getResources(RESOURCETYPE_RAM);
		if (res.size() > 1) throw new Net2PlanException ("Format error");
		if (res.isEmpty()) 
			res.add(n.getNetPlan().addResource(RESOURCETYPE_RAM, RESOURCETYPE_RAM, n, totalRamGB, "GB", new HashMap<> (), 0.0, null));
		else 
			res.iterator().next().setCapacity(totalRamGB, new HashMap<> ());
	}
	public double getTotalHdGB () { return n.getResources(RESOURCETYPE_HD).stream().mapToDouble(r->r.getCapacity()).sum (); }
	public void setTotalHdGB (double totalHdGB) 
	{ 
		final Set<Resource> res = n.getResources(RESOURCETYPE_HD);
		if (res.size() > 1) throw new Net2PlanException ("Format error");
		if (res.isEmpty()) 
			res.add(n.getNetPlan().addResource(RESOURCETYPE_HD, RESOURCETYPE_HD, n, totalHdGB, "GB", new HashMap<> (), 0.0, null));
		else 
			res.iterator().next().setCapacity(totalHdGB, new HashMap<> ());
	}
	public double getOccupiedCpus () { return getCpuBaseResource().getOccupiedCapacity(); } 
	public double getOccupiedHd () { return getHdBaseResource().getOccupiedCapacity(); } 
	public double getOccupiedRam () { return getRamBaseResource().getOccupiedCapacity(); } 
	
	
	public boolean isUp () { return n.isUp(); }

	public SortedSet<WFiber> getOutgoingFibers () { return n.getOutgoingLinks(getNet().getWdmLayer().getNe()).stream().map(ee->new WFiber(ee)).collect(Collectors.toCollection(TreeSet::new)); }
	public SortedSet<WFiber> getIncomingFibers () { return n.getIncomingLinks(getNet().getWdmLayer().getNe()).stream().map(ee->new WFiber(ee)).collect(Collectors.toCollection(TreeSet::new)); }
	Link getIncomingLinkFromAnycastOrigin () 
	{ 
		return n.getNetPlan().getNodePairLinks(getNet().getAnycastOriginNode().getNe(), n, false, getIpNpLayer()).stream().findFirst().orElseThrow(()->new RuntimeException()); 
	}
	Link getOutgoingLinkToAnycastDestination () 
	{ 
		return n.getNetPlan().getNodePairLinks(n , getNet().getAnycastDestinationNode().getNe(), false, getIpNpLayer()).stream().findFirst().orElseThrow(()->new RuntimeException()); 
	}
	
	public void setAsUp () 
	{ 
		n.setFailureState(true);
		final SortedSet<WLightpathRequest> affectedDemands = new TreeSet<> ();
		getOutgoingFibers().forEach(f->affectedDemands.addAll(f.getTraversingLpRequestsInAtLeastOneLp()));
		getIncomingFibers().forEach(f->affectedDemands.addAll(f.getTraversingLpRequestsInAtLeastOneLp()));
		for (WLightpathRequest lpReq : affectedDemands)
			lpReq.internalUpdateOfRoutesCarriedTrafficFromFailureState();
	}
	public void setAsDown () 
	{ 
		n.setFailureState(false);
		final SortedSet<WLightpathRequest> affectedDemands = new TreeSet<> ();
		getOutgoingFibers().forEach(f->affectedDemands.addAll(f.getTraversingLpRequestsInAtLeastOneLp()));
		getIncomingFibers().forEach(f->affectedDemands.addAll(f.getTraversingLpRequestsInAtLeastOneLp()));
		for (WLightpathRequest lpReq : affectedDemands)
			lpReq.internalUpdateOfRoutesCarriedTrafficFromFailureState();
	}

	public void remove () { this.setAsDown(); n.remove(); }
	
}
