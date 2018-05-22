
//CONTINUACION:
//	1. ADD SERVICE CHAIN 
//	2. IMPORTAR DESDE EXCEL --> CON JOSE LUIS
//	2. HACER GETKSHORTESTPATH PARA UN SERVICE CHAIN REQUEST DADA
//	4. METER ALGORITMO GENERICO NO FILTERLESS

package com.net2plan.research.metrohaul.networkModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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

public class WNet extends WAbstractNetworkElement
{
	private static final String ATTNAME_VNFTYPELIST = "VnfTypeListMatrix";
	private static final String ATTNAME_USERSERVICELIST = "userServiceListMatrix";

	public WNet (NetPlan np) { super (np); this.np = np; }
	public final NetPlan np;

	@Override
	public NetPlan getNe() { return (NetPlan) e;  }

	public WLayerWdm getWdmLayer () { return new WLayerWdm(np.getNetworkLayer(0)); }
	public WLayerWdm getIpLayer () { return new WLayerWdm(np.getNetworkLayer(1)); }
	public List<WNode> getNodes () { return np.getNodes().stream().map(n->new WNode(n)).filter(n->!n.isVirtualNode()).collect(Collectors.toCollection(ArrayList::new));  }
	public List<WFiber> getFibers () { return np.getLinks(getWdmLayer().getNe()).stream().map(n->new WFiber(n)).collect(Collectors.toCollection(ArrayList::new));  }
	public List<WLightpathRequest> getLightpathRequests () { return np.getDemands(getWdmLayer().getNe()).stream().map(n->new WLightpathRequest(n)).collect(Collectors.toCollection(ArrayList::new));  }
	public List<WLightpathUnregenerated> getLightpaths () { return np.getRoutes(getWdmLayer().getNe()).stream().map(n->new WLightpathUnregenerated(n)).collect(Collectors.toCollection(ArrayList::new));  }
	
	public void saveToFile (File f) { getNetPlan().saveToFile(f); }

	public static WNet createEmptyDesign ()
	{
		final NetPlan np = new NetPlan ();
		np.addLayer("IP", "IP Layer", "", "", null, null);
		np.addNode(0, 0, WNetConstants.WNODE_NAMEOFANYCASTORIGINNODE , null);
		np.addNode(0, 0, WNetConstants.WNODE_NAMEOFANYCASTDESTINATION, null);
		final WNet res = new WNet (np);
		return res;
	}
	
	public static WNet loadFromFile (File f) { return new WNet (NetPlan.loadFromFile(f));  }
	
	WNode getAnycastOriginNode () { return np.getNode(0) != null ? new WNode (np.getNode(0)) : null; }

	WNode getAnycastDestinationNode () { return np.getNode(1) != null ? new WNode (np.getNode(1)) : null; }
	
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
	
	public Pair<WFiber,WFiber> addFiber (WNode a , WNode b , List<Integer> validOpticalSlotRanges , double lengthInKm , boolean isBidirectional)
	{
		final SortedSet<Integer> opticalSlots = WFiber.computeValidOpticalSlotIds(validOpticalSlotRanges);
		if (isBidirectional)
		{
			final Pair<Link,Link> ee = getNetPlan().addLinkBidirectional(a.getNe(), b.getNe(), opticalSlots.size(), lengthInKm, WNetConstants.WFIBER_DEFAULT_PROPAGATIONSPEEDKMPERSEC, null, getWdmLayer().getNe());
			return Pair.of(new WFiber(ee.getFirst()) , new WFiber(ee.getSecond()));
		}
		else
		{
			final Link ee = getNetPlan().addLink(a.getNe(), b.getNe(), opticalSlots.size(), lengthInKm, WNetConstants.WFIBER_DEFAULT_PROPAGATIONSPEEDKMPERSEC, null, getWdmLayer().getNe());
			return Pair.of(new WFiber(ee) , null);
		}
	}

	public WLightpathRequest addLightpathRequest (WNode a , WNode b , double lineRateGbps , boolean isToBe11Protected)
	{
		final WLightpathRequest lpReq = new WLightpathRequest(getNetPlan().addDemand(a.getNe(), b.getNe(), lineRateGbps, null, getWdmLayer().getNe()));
		lpReq.setIsToBe11Protected(isToBe11Protected);
		return lpReq;
	}
	
	public SortedSet<WNode> getNodesConnectedToCore () { return getNodes ().stream().filter(n->n.isConnectedToNetworkCore()).collect(Collectors.toCollection(TreeSet::new)); }
	
	public WServiceChainRequest addServiceChainRequest (WNode userInjectionNode , boolean isUpstream , WUserService userService)
	{
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

	public WIpLink addIpLink (WNode a , WNode b , double nominalLineRateGbps , boolean isBidirectional)
	{
		if (isBidirectional)
		{
			final Pair<Link,Link> ee = getNetPlan().addLinkBidirectional(a.getNe(), b.getNe(), nominalLineRateGbps, 1, WNetConstants.WFIBER_DEFAULT_PROPAGATIONSPEEDKMPERSEC, null, getIpLayer().getNe());
			return new WIpLink(ee.getFirst());
		}
		else
		{
			final Link ee = getNetPlan().addLink(a.getNe(), b.getNe(), nominalLineRateGbps, 1, WNetConstants.WFIBER_DEFAULT_PROPAGATIONSPEEDKMPERSEC, null, getIpLayer().getNe());
			return new WIpLink(ee);
		}
	}

	public Optional<WNode> getNodeByName (String name) 
	{ 
		return getNodes().stream().filter(n->n.getName().equals(name)).findFirst(); 
	} 
	
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

	public void addOrUpdateVnfType (WVnfType info)
	{
		final SortedMap<String , WVnfType> newInfo = this.getVnfTypesMap();
		newInfo.put(info.getVnfTypeName(), info);
		this.setVnfTypesMap(newInfo);
	}
	
	public void removeVnfType (String vnfTypeName)
	{
		final SortedMap<String , WVnfType> newInfo = this.getVnfTypesMap();
		newInfo.remove(vnfTypeName);
		this.setVnfTypesMap(newInfo);
	}

	public void setVnfTypesMap (SortedMap<String , WVnfType> newInfo)
	{
		final List<List<String>> matrix = new ArrayList<> ();
		for (Entry<String , WVnfType> entry : newInfo.entrySet())
		{
			final List<String> infoThisVnf = new LinkedList<> ();
			infoThisVnf.add(entry.getKey());
			infoThisVnf.add(entry.getValue().getMaxInputTrafficPerVnfInstance_Gbps() + "");
			infoThisVnf.add(entry.getValue().getOccupCpu() + "");
			infoThisVnf.add(entry.getValue().getOccupRam() + "");
			infoThisVnf.add(entry.getValue().getOccupHd() + "");
			infoThisVnf.add(new Boolean (entry.getValue().isConstrained()).toString());
			infoThisVnf.add(entry.getValue().getValidMetroNodesForInstantiation().stream().collect(Collectors.joining(WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER)));
			infoThisVnf.add(entry.getValue().getArbitraryParamString());
			matrix.add(infoThisVnf);
		}
		np.setAttributeAsStringMatrix(ATTNAME_VNFTYPELIST, matrix);
	}
	
	public SortedSet<String> getVnfTypeNames () { return new TreeSet<> (getVnfTypesMap().keySet()); }

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

	public void addOrUpdateUserService (WUserService info)
	{
		final SortedMap<String , WUserService> newInfo = this.getUserServicesInfo();
		newInfo.put(info.getUserServiceUniqueId(), info);
		this.setUserServicesInfo(newInfo);
	}
	
	public void removeUserServiceInfo (String userServiceName)
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
			infoThisVnf.add(entry.getValue().getListMaxLatencyFromInitialToVnfStart_ms_upstream().stream().map(n->n.toString()).collect(Collectors.joining(WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER)) + "");
			infoThisVnf.add(entry.getValue().getListMaxLatencyFromInitialToVnfStart_ms_downstream().stream().map(n->n.toString()).collect(Collectors.joining(WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER)) + "");
			infoThisVnf.add(entry.getValue().getInjectionDownstreamExpansionFactorRespecToBaseTrafficUpstream() + "");
			infoThisVnf.add(new Boolean (entry.getValue().isEndingInCoreNode()).toString());
			infoThisVnf.add(entry.getValue().getArbitraryParamString());
			matrix.add(infoThisVnf);
		}
		np.setAttributeAsStringMatrix(ATTNAME_USERSERVICELIST, matrix);
	}
	
	public SortedSet<String> getUserServiceNames () { return new TreeSet<> (getUserServicesInfo().keySet()); }

	static void ex (String s) { throw new Net2PlanException (s); } 

	public SortedSet<WVnfInstance> getVnfInstances (String type)
	{
		return np.getResources(type).stream().map(r->new WVnfInstance(r)).collect(Collectors.toCollection(TreeSet::new));
	}
	
	public List<List<? extends WAbstractNetworkElement>> getKShortestServiceChainInIpLayer (int k , WNode a , WNode b , List<String> sequenceOfVnfTypesToTraverse ,
			Optional<Map<WIpLink,Double>> optionalCostMapOrElseLatency , Optional<Map<WVnfInstance,Double>> optionalVnfCostMapOrElseLatency)
	{
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
	
	public List<List<WFiber>> getKShortestWdmPath (int k , WNode a , WNode b , Optional<Map<WFiber,Double>> optionalCostMapOrElseLatency)
	{
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
		}
		final List<List<Link>> npRes = GraphUtils.getKLooplessShortestPaths(nodes, links, a.getNe(), b.getNe(), 
				linkCostMap, k, 
				-1, -1, -1, -1, -1, -1);
		final List<List<WFiber>> res = new ArrayList<> (npRes.size());
		for (List<Link> npPath : npRes) res.add(npPath.stream().map(e->new WFiber(e)).collect(Collectors.toList()));
		return res;
	}
}
