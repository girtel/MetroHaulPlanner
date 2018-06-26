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

/** This class represents a node in the network, capable of initiating or ending IP and WDM links, as well as lightpaths and service chains
 */
/**
 * @author Pablo
 *
 */
public class WNode extends WAbstractNetworkElement
{
	private static final String ATTNAMECOMMONPREFIX = "Node_";
	private static final String ATTNAMESUFFIX_TYPE = "type";
	private static final String ATTNAMESUFFIX_ISCONNECTEDTOCORE = "isConnectedToNetworkCore";
	private static final String RESOURCETYPE_CPU = "CPU";
	private static final String RESOURCETYPE_RAM = "RAM";
	private static final String RESOURCETYPE_HD = "HD";
	private static final String ATTNAMESUFFIX_ARBITRARYPARAMSTRING = "ArbitraryString";
	public void setArbitraryParamString (String s) { getNe().setAttribute(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_ARBITRARYPARAMSTRING, s); }
	public String getArbitraryParamString () { return getNe().getAttribute(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_ARBITRARYPARAMSTRING , ""); }
	
	private final Node n;
	
	WNode (Node n) { super (n); this.n = n; }

	
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
	
	boolean isVirtualNode () { return n.getIndex() <= 1; }

	public Node getNe () { return (Node) e; }

	/** Returns the node name, which must be unique among all the nodes
	 * @return
	 */
	public String getName () { return n.getName(); }
	
	/** Sets the node name, which must be unique among all the nodes
	 * @param name
	 */
	public void setName (String name) 
	{ 
		if (name == null) WNet.ex("Names cannot be null");
		if (name.contains(WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER)) throw new Net2PlanException("Names cannot contain the character: " + WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER);  
		if (getNet().getNodes().stream().anyMatch(n->n.getName().equals(name))) WNet.ex("Names cannot be repeated");
		if (name.contains(" ")) throw new Net2PlanException("Names cannot contain spaces");  
		n.setName(name); 
	}
	/** Returns the (X,Y) node position
	 * @return
	 */
	public Point2D getNodePositionXY () { return n.getXYPositionMap(); }
	/** Sets the (X,Y) node position
	 * @param position
	 */
	public void setNodePositionXY (Point2D position) { n.setXYPositionMap(position); }
	/** Returns the user-defined node type
	 * @return
	 */
	public String getType () { return getAttributeOrDefault(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_TYPE , ""); }
	/** Sets the user-defined node type
	 * @param type
	 */
	public void setType (String type) { n.setAttribute(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_TYPE , type); }
	/** Returns if this node is connected to a core node (core nodes are not in the design)
	 * @return
	 */
	public boolean isConnectedToNetworkCore () { return getAttributeAsBooleanOrDefault(ATTNAMECOMMONPREFIX +ATTNAMESUFFIX_ISCONNECTEDTOCORE , WNetConstants.WNODE_DEFAULT_ISCONNECTEDTOCORE); }
	/** Sets if this node is assumed to be connected to a core node (core nodes are not in the design)
	 * @param isConnectedToCore
	 */
	public void setIsConnectedToNetworkCore (boolean isConnectedToCore) { n.setAttribute(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_ISCONNECTEDTOCORE , new Boolean (isConnectedToCore).toString()); }
	/** Returns the user-defined node population
	 * @return
	 */
	public double getPopulation () { return n.getPopulation(); }
	/** Sets the user-defined node population
	 * @param population
	 */
	public void setPoputlation (double population) { n.setPopulation(population); }
	/** Returns the number of CPUs available in the node for instantiation of VNFs
	 * @return
	 */
	public double getTotalNumCpus () { return n.getResources(RESOURCETYPE_CPU).stream().mapToDouble(r->r.getCapacity()).sum (); }
	/** Sets the number of CPUs available in the node for instantiation of VNFs
	 * @param totalNumCpus
	 */
	public void setTotalNumCpus (double totalNumCpus) 
	{ 
		final Set<Resource> res = n.getResources(RESOURCETYPE_CPU);
		if (res.size() > 1) throw new Net2PlanException ("Format error");
		if (res.isEmpty()) 
			res.add(n.getNetPlan().addResource(RESOURCETYPE_CPU, RESOURCETYPE_CPU, n, totalNumCpus, RESOURCETYPE_CPU, new HashMap<> (), 0.0, null));
		else 
			res.iterator().next().setCapacity(totalNumCpus, new HashMap<> ());
	}
	/** Returns the total RAM (in GBytes) available in the node for instantiation of VNFs
	 * @return
	 */
	public double getTotalRamGB () { return n.getResources(RESOURCETYPE_RAM).stream().mapToDouble(r->r.getCapacity()).sum (); }
	/** Sets the total RAM (in GBytes) available in the node for instantiation of VNFs
	 * @return
	 */
	public void setTotalRamGB (double totalRamGB) 
	{ 
		final Set<Resource> res = n.getResources(RESOURCETYPE_RAM);
		if (res.size() > 1) throw new Net2PlanException ("Format error");
		if (res.isEmpty()) 
			res.add(n.getNetPlan().addResource(RESOURCETYPE_RAM, RESOURCETYPE_RAM, n, totalRamGB, "GB", new HashMap<> (), 0.0, null));
		else 
			res.iterator().next().setCapacity(totalRamGB, new HashMap<> ());
	}
	/** Returns the total hard disk size (in GBytes) available in the node for instantiation of VNFs
	 * @return
	 */
	public double getTotalHdGB () { return n.getResources(RESOURCETYPE_HD).stream().mapToDouble(r->r.getCapacity()).sum (); }
	/** Sets the total hard disk size (in GBytes) available in the node for instantiation of VNFs
	 * @return
	 */
	public void setTotalHdGB (double totalHdGB) 
	{ 
		final Set<Resource> res = n.getResources(RESOURCETYPE_HD);
		if (res.size() > 1) throw new Net2PlanException ("Format error");
		if (res.isEmpty()) 
			res.add(n.getNetPlan().addResource(RESOURCETYPE_HD, RESOURCETYPE_HD, n, totalHdGB, "GB", new HashMap<> (), 0.0, null));
		else 
			res.iterator().next().setCapacity(totalHdGB, new HashMap<> ());
	}
	/** Returns the current number of occupied CPUs by the instantiated VNFs
	 * @return
	 */
	public double getOccupiedCpus () { return getCpuBaseResource().getOccupiedCapacity(); } 
	/** Returns the current amount of occupied hard-disk (in giga bytes) by the instantiated VNFs
	 * @return
	 */
	public double getOccupiedHdGB () { return getHdBaseResource().getOccupiedCapacity(); } 
	/** Returns the current amount of occupied RAM (in giga bytes) by the instantiated VNFs
	 * @return
	 */
	public double getOccupiedRamGB () { return getRamBaseResource().getOccupiedCapacity(); } 
	
	
	/** Indicates if the node is up or down (failed)
	 * @return
	 */
	public boolean isUp () { return n.isUp(); }

	/** Returns the set of outgoing fibers of the node
	 * @return
	 */
	public SortedSet<WFiber> getOutgoingFibers () { return n.getOutgoingLinks(getNet().getWdmLayer().getNe()).stream().map(ee->new WFiber(ee)).collect(Collectors.toCollection(TreeSet::new)); }
	/** Returns the set of incoming fibers to the node
	 * @return
	 */
	public SortedSet<WFiber> getIncomingFibers () { return n.getIncomingLinks(getNet().getWdmLayer().getNe()).stream().map(ee->new WFiber(ee)).collect(Collectors.toCollection(TreeSet::new)); }
	
	Link getIncomingLinkFromAnycastOrigin () 
	{ 
		return n.getNetPlan().getNodePairLinks(getNet().getAnycastOriginNode().getNe(), n, false, getIpNpLayer()).stream().findFirst().orElseThrow(()->new RuntimeException()); 
	}
	Link getOutgoingLinkToAnycastDestination () 
	{ 
		return n.getNetPlan().getNodePairLinks(n , getNet().getAnycastDestinationNode().getNe(), false, getIpNpLayer()).stream().findFirst().orElseThrow(()->new RuntimeException()); 
	}
	
	/** Sets the node as up (working, non-failed)
	 */
	public void setAsUp () 
	{ 
		n.setFailureState(true);
		final SortedSet<WLightpathRequest> affectedDemands = new TreeSet<> ();
		getOutgoingFibers().forEach(f->affectedDemands.addAll(f.getTraversingLpRequestsInAtLeastOneLp()));
		getIncomingFibers().forEach(f->affectedDemands.addAll(f.getTraversingLpRequestsInAtLeastOneLp()));
		for (WLightpathRequest lpReq : affectedDemands)
			lpReq.internalUpdateOfRoutesCarriedTrafficFromFailureState();
	}
	
	/** Sets the node as down (failed), so traversing IP links or lightpaths become down
	 */
	public void setAsDown () 
	{ 
		n.setFailureState(false);
		final SortedSet<WLightpathRequest> affectedDemands = new TreeSet<> ();
		getOutgoingFibers().forEach(f->affectedDemands.addAll(f.getTraversingLpRequestsInAtLeastOneLp()));
		getIncomingFibers().forEach(f->affectedDemands.addAll(f.getTraversingLpRequestsInAtLeastOneLp()));
		for (WLightpathRequest lpReq : affectedDemands)
			lpReq.internalUpdateOfRoutesCarriedTrafficFromFailureState();
	}

	/** Removes this node, and all the ending & initiated links, or traversing lightpaths or service chains 
	 * 
	 */
	public void remove () { this.setAsDown(); n.remove(); }
	
}
