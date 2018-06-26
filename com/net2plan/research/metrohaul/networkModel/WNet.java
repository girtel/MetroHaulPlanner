
package com.net2plan.research.metrohaul.networkModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.net2plan.interfaces.networkDesign.Demand;
import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.NetworkElement;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.interfaces.networkDesign.Resource;
import com.net2plan.libraries.GraphUtils;
import com.net2plan.utils.Pair;
import com.net2plan.utils.StringUtils;

import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;

/** This class represents an IP over WDM network with potential VNF placement. This is the main model class, that gives access 
 * to the key methods to play with the network
 *
 */
public class WNet extends WAbstractNetworkElement
{
	private static final String ATTNAME_VNFTYPELIST = "VnfTypeListMatrix";
	private static final String ATTNAME_USERSERVICELIST = "userServiceListMatrix";

	public WNet (NetPlan np)
	{
		super (np);
		this.np = np;
		if(np.getNumberOfNodes() < 2 || np.getNumberOfLayers() != 2) throw new Net2PlanException("Incorrect number of nodes or layers");
	}
	private final NetPlan np;

	@Override
	public NetPlan getNe() { return (NetPlan) e;  }

	/** Returns the object identifying the WDM layer
	 * @return
	 */
	public WLayerWdm getWdmLayer () { return new WLayerWdm(np.getNetworkLayer(0)); }
	/** Returns the object identifying the IP layer
	 * @return
	 */
	public WLayerIp getIpLayer () { return new WLayerIp(np.getNetworkLayer(1)); }
	
	/** Returns the list of network nodes, in increasing order according to its id
	 * @return
	 */
	public List<WNode> getNodes () { return np.getNodes().stream().map(n->new WNode(n)).filter(n->!n.isVirtualNode()).collect(Collectors.toCollection(ArrayList::new));  }
	/** Returns the list of fibers, in increasing order according to its id
	 * @return
	 */
	public List<WFiber> getFibers () { return np.getLinks(getWdmLayer().getNe()).stream().map(n->new WFiber(n)).collect(Collectors.toCollection(ArrayList::new));  }
	/** Returns the list of lightpath requests, in increasing order according to its id
	 * @return
	 */
	public List<WLightpathRequest> getLightpathRequests () { return np.getDemands(getWdmLayer().getNe()).stream().map(n->new WLightpathRequest(n)).collect(Collectors.toCollection(ArrayList::new));  }
	/** Returns the list of lightpaths, in increasing order according to its id
	 * @return
	 */
	public List<WLightpathUnregenerated> getLightpaths () { return np.getRoutes(getWdmLayer().getNe()).stream().map(n->new WLightpathUnregenerated(n)).collect(Collectors.toCollection(ArrayList::new));  }
	
	/** Returns the list of service chain requests, in increasing order according to its id
	 * @return
	 */
	public List<WServiceChainRequest> getServiceChainRequests () { return np.getDemands(getIpLayer().getNe()).stream().map(n->new WServiceChainRequest(n)).collect(Collectors.toCollection(ArrayList::new));  }

	
	/** Saves this network in the given file
	 * @param f
	 */
	public void saveToFile (File f) { getNetPlan().saveToFile(f); }

	/** Creates an empty design with no nodes, links etc.
	 * @return
	 */
	public static WNet createEmptyDesign ()
	{
		final NetPlan np = new NetPlan ();
		np.addLayer("IP", "IP Layer", "", "", null, null);
		np.addNode(0, 0, WNetConstants.WNODE_NAMEOFANYCASTORIGINNODE , null);
		np.addNode(0, 0, WNetConstants.WNODE_NAMEOFANYCASTDESTINATION, null);
		final WNet res = new WNet (np);
		return res;
	}
	
	/** Creates a design, loading it from a file
	 * @param f
	 * @return
	 */
	public static WNet loadFromFile (File f) { return new WNet (NetPlan.loadFromFile(f));  }
	
	WNode getAnycastOriginNode () { return np.getNode(0) != null ? new WNode (np.getNode(0)) : null; }

	WNode getAnycastDestinationNode () { return np.getNode(1) != null ? new WNode (np.getNode(1)) : null; }
	
	/** Adds a node to the design
	 * @param xCoord the x coordinate
	 * @param yCoord the y coordinate
	 * @param name the node name, that should be unique
	 * @param type the type of node: a user-defined string
	 * @return
	 */
	public WNode addNode (double xCoord, double yCoord, String name , String type)
	{
		if (name == null) ex("Names cannot be null");
		if (name.contains(WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER)) throw new Net2PlanException("Names cannot contain the character: " + WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER);  
		if (getNodes().stream().anyMatch(n->n.getName().equals(name))) ex("Names cannot be repeated");
		final WNode n = new WNode (getNetPlan().addNode(xCoord, yCoord, name, null));
		n.setType(type);
		addIpLink(getAnycastOriginNode(), n, Double.MAX_VALUE, false);
		addIpLink(n , getAnycastDestinationNode(), Double.MAX_VALUE, false);
		return n;
	}
	
	/** Adds a fiber to the design
	 * @param a the origin node
	 * @param b the destination node
	 * @param validOpticalSlotRanges the pairs (init slot , end slot) with the valid optical slot ranges for the fiber
	 * @param lengthInKm the user-defined length in km. If negative, the harversine distance is set for the link length, that considers node (x,y) coordinates a longitude-latitude pairs 
	 * @param isBidirectional indicates if the fiber to add is bidirectional
	 * @return the first element of the pair is the fiber a->b, the second is b->a or null in non-bidirectional cases
	 */
	public Pair<WFiber,WFiber> addFiber (WNode a , WNode b , List<Integer> validOpticalSlotRanges , double lengthInKm , boolean isBidirectional)
	{
		checkInThisWNet (a , b);
		if (lengthInKm < 0) lengthInKm = getNetPlan().getNodePairEuclideanDistance(a.getNe(), b.getNe());
		final SortedSet<Integer> opticalSlots = WFiber.computeValidOpticalSlotIds(validOpticalSlotRanges);
		if (isBidirectional)
		{
			final Pair<Link,Link> ee = getNetPlan().addLinkBidirectional(a.getNe(), b.getNe(), opticalSlots.size(), lengthInKm, WNetConstants.WFIBER_DEFAULT_PROPAGATIONSPEEDKMPERSEC, null, getWdmLayer().getNe());
			WFiber fiber1 = new WFiber(ee.getFirst()); fiber1.setValidOpticalSlotRanges(validOpticalSlotRanges);
			WFiber fiber2 = new WFiber(ee.getSecond()); fiber2.setValidOpticalSlotRanges(validOpticalSlotRanges);
			return Pair.of(fiber1, fiber2);
		}
		else
		{
			final Link ee = getNetPlan().addLink(a.getNe(), b.getNe(), opticalSlots.size(), lengthInKm, WNetConstants.WFIBER_DEFAULT_PROPAGATIONSPEEDKMPERSEC, null, getWdmLayer().getNe());
			WFiber fiber = new WFiber(ee);
			fiber.setValidOpticalSlotRanges(validOpticalSlotRanges);
			return Pair.of(fiber, null);
		}
	}

	/** Adds a lightpath request to the design
	 * @param a The origin node of the lightpath request
	 * @param b The destination node of the lightpath request
	 * @param lineRateGbps The nominal line rate in Gbps
	 * @param isToBe11Protected indicates if it is requested to have a 1+1 setting assigned to this lightpath 
	 * @return the lightpath request created
	 */
	public WLightpathRequest addLightpathRequest (WNode a , WNode b , double lineRateGbps , boolean isToBe11Protected)
	{
		checkInThisWNet (a , b);
		final WLightpathRequest lpReq = new WLightpathRequest(getNetPlan().addDemand(a.getNe(), b.getNe(), lineRateGbps, null, getWdmLayer().getNe()));
		lpReq.setIsToBe11Protected(isToBe11Protected);
		return lpReq;
	}
	
	/** Returns the nodes in the network that are supposed to have a connection to the network core (the core nodes and those connecting links are not part of the network)
	 * @return
	 */
	public SortedSet<WNode> getNodesConnectedToCore () { return getNodes ().stream().filter(n->n.isConnectedToNetworkCore()).collect(Collectors.toCollection(TreeSet::new)); }
	
	/** Adds a request for an upstream or downstream unidirectional service chain, for the given user service. The user service will define the sequence of VNF types that 
	 * the flow must traverse, and will indicate the set of potential end nodes (for upstream flows) or potential initial nodes (for downstream flows). 
	 * @param userInjectionNode the node where the traffic starts (in upstream flows) or ends (in downstream flows)
	 * @param isUpstream if true, the flow is supposed to start in the injection node, if false, the flow is supposed to end in the injection node
	 * @param userService the user service defining this service chain request. 
	 * @return see above
	 */
	public WServiceChainRequest addServiceChainRequest (WNode userInjectionNode , boolean isUpstream , WUserService userService)
	{
		checkInThisWNet (userInjectionNode);
		final Demand scNp = getNetPlan().addDemand(getAnycastOriginNode().getNe(), 
				getAnycastDestinationNode().getNe(), 
				0.0, 
				null, 
				getIpNpLayer());
		final WServiceChainRequest scReq = new WServiceChainRequest(scNp);
		scReq.setUserServiceName(userService.getUserServiceUniqueId());
		if (isUpstream)
		{
			scNp.setServiceChainSequenceOfTraversedResourceTypes(userService.getListVnfTypesToTraverseUpstream());
			scReq.setIsUpstream(true);
			scReq.setPotentiallyValidOrigins(Sets.newTreeSet(Arrays.asList(userInjectionNode)));
			if (userService.isEndingInCoreNode ())
				scReq.setPotentiallyValidDestinations(getNodesConnectedToCore ());
			else
				scReq.setPotentiallyValidDestinations(new TreeSet<> ());
			scReq.setSequenceOfExpansionFactorsRespectToInjection(userService.getSequenceTrafficExpansionFactorsRespectToBaseTrafficUpstream());
			scReq.setListMaxLatencyFromInitialToVnfStart_ms(userService.getListMaxLatencyFromInitialToVnfStart_ms_upstream());
		}
		else
		{
			scNp.setServiceChainSequenceOfTraversedResourceTypes(userService.getListVnfTypesToTraverseDownstream());
			scReq.setIsUpstream(false);
			scReq.setPotentiallyValidDestinations(Sets.newTreeSet(Arrays.asList(userInjectionNode)));
			if (userService.isEndingInCoreNode ())
				scReq.setPotentiallyValidOrigins(getNodesConnectedToCore ());
			else
				scReq.setPotentiallyValidOrigins(new TreeSet<> ());
			scReq.setSequenceOfExpansionFactorsRespectToInjection(userService.getSequenceTrafficExpansionFactorsRespectToBaseTrafficDownstream());
			scReq.setListMaxLatencyFromInitialToVnfStart_ms(userService.getListMaxLatencyFromInitialToVnfStart_ms_downstream());
		}
		return scReq;
	}

	/** Adds an IP link to the network 
	 * @param a The IP link origin node
	 * @param b The IP lin kend node
	 * @param nominalLineRateGbps The nominal line rate of the IP link in Gbps
	 * @param isBidirectional An indication if the IP link to add should be a pair of two unidirectional IP links in opposite directions
	 * @return the first element of the pair is the a->b IP link, the second is the b->a IP link or null if not bidirectional IP link is requested
	 */
	public WIpLink addIpLink (WNode a , WNode b , double nominalLineRateGbps , boolean isBidirectional)
	{
		checkInThisWNet (a , b);
		if (isBidirectional)
		{
			final Pair<Link,Link> ee = getNetPlan().addLinkBidirectional(a.getNe(), b.getNe(), nominalLineRateGbps, 1, WNetConstants.WFIBER_DEFAULT_PROPAGATIONSPEEDKMPERSEC, null, getIpLayer().getNe());
			WIpLink ipLink1 = new WIpLink(ee.getFirst());
			WIpLink ipLink2 = new WIpLink(ee.getSecond());
			return ipLink1;
		}
		else
		{
			final Link ee = getNetPlan().addLink(a.getNe(), b.getNe(), nominalLineRateGbps, 1, WNetConstants.WFIBER_DEFAULT_PROPAGATIONSPEEDKMPERSEC, null, getIpLayer().getNe());
			WIpLink ipLink = new WIpLink(ee);
			ipLink.setNominalCapacityInGbps(nominalLineRateGbps);
			return ipLink;
		}
	}

	/** Returns the network node with the indicated name, if any
	 * @param name
	 * @return
	 */
	public Optional<WNode> getNodeByName (String name) 
	{ 
		return getNodes().stream().filter(n->n.getName().equals(name)).findFirst(); 
	} 
	
	/** Returns the map associating the VNF type, with the associated VNF object, as was defined by the user 
	 * @return
	 */
	public SortedMap<String , WVnfType> getVnfTypesMap ()
	{
		final SortedMap<String , WVnfType> res = new TreeMap<> ();
		final List<List<String>> matrix = getNe().getAttributeAsStringMatrix(ATTNAME_VNFTYPELIST, null);
		if (matrix == null) throw new Net2PlanException("Wrong format");
		for (List<String> row : matrix)
		{
			if (row.size() != 8) throw new Net2PlanException ("Wrong format"); 
			final String vnfTypeName = row.get(0);
			if (vnfTypeName.contains(WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER)) throw new Net2PlanException ("VNF type names cannot contain the character: " + WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER); 
			if (res.containsKey(vnfTypeName)) throw new Net2PlanException ("VNF type names must be unique");
			final double maxInputTraffic_Gbps = Double.parseDouble(row.get(1));
			final double numCpus = Double.parseDouble(row.get(2));
			final double numRam = Double.parseDouble(row.get(3));
			final double numHd = Double.parseDouble(row.get(4));
			final boolean isConstrained = Boolean.parseBoolean(row.get(5));
			final SortedSet<String> nodeNames = new TreeSet<> (Arrays.asList(row.get(6).split(WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER)));
			final String arbitraryParamString = row.get(7);
			res.put(vnfTypeName, new WVnfType(vnfTypeName, maxInputTraffic_Gbps , numCpus, numRam, numHd , isConstrained , nodeNames , arbitraryParamString));
		}
		return res;
	}

	/** Adds a new VNF type to the network, with the provided information
	 * @param info
	 */
	public void addOrUpdateVnfType (WVnfType info)
	{
		SortedMap<String , WVnfType> newInfo = new TreeMap<> ();
		if(getNe().getAttributeAsStringMatrix(ATTNAME_VNFTYPELIST, null) != null){newInfo = this.getVnfTypesMap();}
		newInfo.put(info.getVnfTypeName(), info);
		this.setVnfTypesMap(newInfo);
	}
	
	/** Removes a defined VNF type
	 * @param vnfTypeName
	 */
	public void removeVnfType (String vnfTypeName)
	{
		final SortedMap<String , WVnfType> newInfo = this.getVnfTypesMap();
		newInfo.remove(vnfTypeName);
		this.setVnfTypesMap(newInfo);
	}

	/** Sets a new VNF type map, removing all previous VNF types defined
	 * @param newInfo
	 */
	public void setVnfTypesMap (SortedMap<String , WVnfType> newInfo)
	{
		final List<List<String>> matrix = new ArrayList<> ();
		for (Entry<String , WVnfType> entry : newInfo.entrySet())
		{
			final List<String> infoThisVnf = new LinkedList<> ();
			infoThisVnf.add(entry.getKey());
			infoThisVnf.add(entry.getValue().getMaxInputTrafficPerVnfInstance_Gbps() + "");
			infoThisVnf.add(entry.getValue().getOccupCpu() + "");
			infoThisVnf.add(entry.getValue().getOccupRamGBytes() + "");
			infoThisVnf.add(entry.getValue().getOccupHdGBytes() + "");
			infoThisVnf.add(new Boolean (entry.getValue().isConstrainedToBeInstantiatedOnlyInUserDefinedNodes()).toString());
			infoThisVnf.add(entry.getValue().getValidMetroNodesForInstantiation().stream().collect(Collectors.joining(WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER)));
			infoThisVnf.add(entry.getValue().getArbitraryParamString());
			matrix.add(infoThisVnf);
		}
		np.setAttributeAsStringMatrix(ATTNAME_VNFTYPELIST, matrix);
	}
	
	/** Returns the set of VNF type names defined
	 * @return
	 */
	public SortedSet<String> getVnfTypeNames () { return new TreeSet<> (getVnfTypesMap().keySet()); }

	/** Returns the user service information defined, in a map with key the user service identifier, and value the user service object 
	 * @return
	 */
	public SortedMap<String , WUserService> getUserServicesInfo ()
	{
		final SortedMap<String , WUserService> res = new TreeMap<> ();
		final List<List<String>> matrix = getNe().getAttributeAsStringMatrix(ATTNAME_USERSERVICELIST, null);
		if (matrix == null) throw new Net2PlanException("Wrong format");
		for (List<String> row : matrix)
		{
			if (row.size() != 10) throw new Net2PlanException ("Wrong format");
			final String userServiceUniqueId = row.get(0);
			if (res.containsKey(userServiceUniqueId)) throw new Net2PlanException ("User service names must be unique");
			final List<String> listVnfTypesToTraverseUpstream = Arrays.asList(StringUtils.split(row.get(1) ,WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER));
			final List<String> listVnfTypesToTraverseDownstream = Arrays.asList(StringUtils.split(row.get(2) ,WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER));
			final List<Double> sequenceTrafficExpansionFactorsRespectToBaseTrafficUpstream = Arrays.asList(row.get(3).split(WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER)).stream().map(s->Double.parseDouble(s)).collect(Collectors.toList());
			final List<Double> sequenceTrafficExpansionFactorsRespectToBaseTrafficDownstream = Arrays.asList(row.get(4).split(WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER)).stream().map(s->Double.parseDouble(s)).collect(Collectors.toList());
			final List<Double> listMaxLatencyFromInitialToVnfStart_ms_upstream = Arrays.asList(row.get(5).split(WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER)).stream().map(s->Double.parseDouble(s)).collect(Collectors.toList());
			final List<Double> listMaxLatencyFromInitialToVnfStart_ms_downstream = Arrays.asList(row.get(6).split(WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER)).stream().map(s->Double.parseDouble(s)).collect(Collectors.toList());
			final double injectionDownstreamExpansionFactorRespecToBaseTrafficUpstream = Double.parseDouble(row.get(7));
			final boolean isEndingInCoreNode = Boolean.parseBoolean(row.get(8));
			final String arbitraryParamString = row.get(9);
			res.put(userServiceUniqueId, 
					new WUserService(userServiceUniqueId, listVnfTypesToTraverseUpstream, 
							listVnfTypesToTraverseDownstream, 
							sequenceTrafficExpansionFactorsRespectToBaseTrafficUpstream, 
							sequenceTrafficExpansionFactorsRespectToBaseTrafficDownstream, 
							listMaxLatencyFromInitialToVnfStart_ms_upstream, 
							listMaxLatencyFromInitialToVnfStart_ms_downstream,
							injectionDownstreamExpansionFactorRespecToBaseTrafficUpstream, 
							isEndingInCoreNode , 
							arbitraryParamString));
		}
		return res;
	}

	/** Adds or updates a user service to the defined repository of user services
	 * @param info
	 */
	public void addOrUpdateUserService (WUserService info)
	{
		SortedMap<String , WUserService> newInfo = new TreeMap<> ();
		if(getNe().getAttributeAsStringMatrix(ATTNAME_USERSERVICELIST, null) != null) newInfo = this.getUserServicesInfo();
		newInfo.put(info.getUserServiceUniqueId(), info);
		this.setUserServicesInfo(newInfo);
	}
	
	/** Removes a user service definition from the internal repo
	 * @param userServiceName
	 */
	public void removeUserServiceInfo (String userServiceName)
	{
		final SortedMap<String , WUserService> newInfo = this.getUserServicesInfo();
		newInfo.remove(userServiceName);
		this.setUserServicesInfo(newInfo);
	}

	/** Sets a new user service id to info map, removing all previous user services defined
	 * @param newInfo
	 */
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
			infoThisVnf.add(entry.getValue().getListMaxLatencyFromInitialToVnfStart_ms_upstream().stream().map(n->n.toString()).collect(Collectors.joining(WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER)) + "");
			infoThisVnf.add(entry.getValue().getListMaxLatencyFromInitialToVnfStart_ms_downstream().stream().map(n->n.toString()).collect(Collectors.joining(WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER)) + "");
			infoThisVnf.add(entry.getValue().getInjectionDownstreamExpansionFactorRespecToBaseTrafficUpstream() + "");
			infoThisVnf.add(new Boolean (entry.getValue().isEndingInCoreNode()).toString());
			infoThisVnf.add(entry.getValue().getArbitraryParamString());
			matrix.add(infoThisVnf);
		}
		np.setAttributeAsStringMatrix(ATTNAME_USERSERVICELIST, matrix);
	}
	
	/** Returns all the unique ids (names) of the user services defined
	 * @return
	 */
	public SortedSet<String> getUserServiceNames () { return new TreeSet<> (getUserServicesInfo().keySet()); }

	static void ex (String s) { throw new Net2PlanException (s); } 

	/** Returns a set with all the VNF instances in the network of the given type
	 * @param type
	 * @return
	 */
	public SortedSet<WVnfInstance> getVnfInstances (String type)
	{
		return np.getResources(type).stream().map(r->new WVnfInstance(r)).collect(Collectors.toCollection(TreeSet::new));
	}
	
	/** Returns a ranking with the k-shortest paths for service chains, given the end nodes, and the sequence of types of VNFs that it should traverse.
	 * @param k the maximum number of paths to return in the ranking
	 * @param a the origin node
	 * @param b the end node
	 * @param sequenceOfVnfTypesToTraverse the sequence of VNF types to be traversed, in the given order
	 * @param optionalCostMapOrElseLatency an optional map of the cost to assign to traversing each IP link, if none, the cost is assumed to the IP link latency 
	 * @param optionalVnfCostMapOrElseLatency an optional map of the cost to assign to traverse each VNF instance, if none, the cost is assumed to be the VNF instance processing time
	 * @return a list of up to k paths, where each path is a sequence of WIpLink and WVnfInstance objects forming a service chain from a to b 
	 */
	public List<List<? extends WAbstractNetworkElement>> getKShortestServiceChainInIpLayer (int k , WNode a , WNode b , List<String> sequenceOfVnfTypesToTraverse ,
			Optional<Map<WIpLink,Double>> optionalCostMapOrElseLatency , Optional<Map<WVnfInstance,Double>> optionalVnfCostMapOrElseLatency)
	{
		checkInThisWNet (a , b);
		if (optionalCostMapOrElseLatency.isPresent()) checkInThisWNetCol(optionalCostMapOrElseLatency.get().keySet()); 
		if (optionalVnfCostMapOrElseLatency.isPresent()) checkInThisWNetCol(optionalVnfCostMapOrElseLatency.get().keySet()); 
		final Node anycastNode_a = getAnycastOriginNode().getNe();
		final Node anycastNode_b = getAnycastDestinationNode().getNe();
		final List<Link> npLinks = np.getLinks(getIpNpLayer()).stream().
				filter(e->!e.getOriginNode().equals(anycastNode_a) && !e.getDestinationNode().equals(anycastNode_b)).
				collect (Collectors.toList());
		final Node originNode = a.getNe ();
		final Node destinationNode = b.getNe ();
		final List<String> sequenceOfResourceTypesToTraverse = sequenceOfVnfTypesToTraverse;
		final DoubleMatrix1D linkCost = DoubleFactory1D.dense.make(npLinks.size());
		for (int cont = 0; cont < npLinks.size() ; cont ++)
		{
			final WIpLink e = new WIpLink (npLinks.get(cont));
			linkCost.set(cont, optionalCostMapOrElseLatency.isPresent()? optionalCostMapOrElseLatency.get().get(e) : e.getWorstCasePropagationDelayInMs());
		}
		final Map<Resource,Double> resourceCost = new HashMap<> ();
		for (String vnfType : new HashSet<> (sequenceOfResourceTypesToTraverse))
		{
			for (WVnfInstance vfnInstance : getVnfInstances(vnfType))
			{
				final double cost = optionalVnfCostMapOrElseLatency.isPresent()? optionalVnfCostMapOrElseLatency.get().get(vfnInstance) : vfnInstance.getProcessingTimeInMs();
				resourceCost.put(vfnInstance.getNe(), cost);
			}
		}
		final List<Pair<List<NetworkElement>,Double>> kShortest = GraphUtils.getKMinimumCostServiceChains(npLinks ,  
				originNode, destinationNode, sequenceOfResourceTypesToTraverse , linkCost, 
				resourceCost , 
				k, -1 , -1, -1, -1, null);
		final List<List<? extends WAbstractNetworkElement>> res = new ArrayList<> (kShortest.size());
		for (Pair<List<NetworkElement>,Double> npPath : kShortest)
		{
			final List<? extends WAbstractNetworkElement> wpath = npPath.getFirst().stream().
					map(e->e instanceof Link? new WIpLink((Link)e) : new WVnfInstance((Resource)e)).
					collect(Collectors.toList());
			res.add(wpath);
		}
		return res;
	}
	
	/** Returns a ranking with the k-shortest paths composed as a sequence of WFiber links in the network, given the origin and end nodes of the paths.
	 * @param k the maximum number of paths to return in the ranking
	 * @param a the origin node
	 * @param b the end node
	 * @param optionalCostMapOrElseLatency an optional map of the cost to assign to traversing each WFiber link, if none, the cost is assumed to be the WFiber latency
	 * @return a list of up to k paths, where each path is a sequence of WFiber objects forming a path from a to b 
	 */
	public List<List<WFiber>> getKShortestWdmPath (int k , WNode a , WNode b , Optional<Map<WFiber,Double>> optionalCostMapOrElseLatency)
	{
		checkInThisWNet (a , b);
		if (optionalCostMapOrElseLatency.isPresent()) checkInThisWNetCol(optionalCostMapOrElseLatency.get().keySet()); 
		
		final List<Node> nodes = np.getNodes();
		final List<Link> links = np.getLinks(getWdmNpLayer());
		final Map<Link, Double> linkCostMap = new HashMap<> ();
		for (Link e : links)
		{
			final double cost;
			if (optionalCostMapOrElseLatency.isPresent())
				cost = optionalCostMapOrElseLatency.get().getOrDefault(new WFiber(e), e.getPropagationDelayInMs());
			else
				cost = e.getPropagationDelayInMs();
			linkCostMap.put(e, cost);
		}
		final List<List<Link>> npRes = GraphUtils.getKLooplessShortestPaths(nodes, links, a.getNe(), b.getNe(), 
				linkCostMap, k,
				-1, -1, -1, -1, -1, -1);
		final List<List<WFiber>> res = new ArrayList<> (npRes.size());
		for (List<Link> npPath : npRes) res.add(npPath.stream().map(e->new WFiber(e)).collect(Collectors.toList()));
		return res;
	}
	
	void checkInThisWNet (WAbstractNetworkElement ...ne)
	{
		for (WAbstractNetworkElement e : ne) if (e.getNetPlan() != this.getNetPlan()) throw new Net2PlanException ("The element does not belong to this network");
	}
	void checkInThisWNetCol (Collection<? extends WAbstractNetworkElement> ne)
	{
		for (WAbstractNetworkElement e : ne) if (e.getNetPlan() != this.getNetPlan()) throw new Net2PlanException ("The element does not belong to this network");
	}
	
}
