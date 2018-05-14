
//CONTINUACION:
//	1. ADD SERVICE CHAIN 
//	2. IMPORTAR DESDE EXCEL --> CON JOSE LUIS
//	2. HACER GETKSHORTESTPATH PARA UN SERVICE CHAIN REQUEST DADA
//	4. METER ALGORITMO GENERICO NO FILTERLESS

package com.net2plan.research.metrohaul.networkModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.net2plan.interfaces.networkDesign.Demand;
import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.utils.Pair;
import com.net2plan.utils.Quadruple;
import com.net2plan.utils.Triple;

public class WNet extends WAbstractNetworkElement
{
	private static final String ATTNAME_VNFTYPELIST = "VnfTypeListMatrix";

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
		np.addNode(0, 0, WNetConstants.WNODE_NAMEOFANYCASTORIGINNODE , null);
		np.addNode(0, 0, WNetConstants.WNODE_NAMEOFANYCASTDESTINATION, null);
		final WNet res = new WNet (new NetPlan ());
		return res;
	}
	
	public static WNet loadFromFile (File f) { return new WNet (NetPlan.loadFromFile(f));  }
	
	WNode getAnycastOriginNode () { return new WNode (np.getNode(0)); }

	WNode getAnycastDestinationNode () { return new WNode (np.getNode(1)); }
	
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
		final List<String> seqVnfTypes = isUpstream? userService.getListVnfTypesToTraverseUpstream() : userService.getListVnfTypesToTraverseDownstream();
		final List<Double> seqExpansionFactors = isUpstream? userService.getSequenceTrafficExpansionFactorsRespectToBaseTrafficUpstream() : userService.getSequenceTrafficExpansionFactorsRespectToBaseTrafficDownstream();
		scNp.setServiceChainSequenceOfTraversedResourceTypes(seqVnfTypes);
		final WServiceChainRequest scReq = new WServiceChainRequest(scNp);
		scReq.setIsUpstream(isUpstream);
		scReq.setUserServiceName(userService.getUserServiceUniqueId());
		scReq.setSequenceOfExpansionFactorsRespectToInjection(seqExpansionFactors);
		if (isUpstream)
		{
			scReq.setPotentiallyValidOrigins(Sets.newTreeSet(Arrays.asList(userInjectionNode)));
			if (userService.isEndingInCoreNode ())
				scReq.setPotentiallyValidDestinations(getNodesConnectedToCore ());
			else
				scReq.setPotentiallyValidDestinations(new TreeSet<> ());
		}
		else
		{
			scReq.setPotentiallyValidDestinations(Sets.newTreeSet(Arrays.asList(userInjectionNode)));
			if (userService.isEndingInCoreNode ())
				scReq.setPotentiallyValidOrigins(getNodesConnectedToCore ());
			else
				scReq.setPotentiallyValidOrigins(new TreeSet<> ());
		}
		return scReq;
	}

	public WIpLink addIpLink (WNode a , WNode b , double nominalLineRateGbps , boolean isBidirectional)
	{
		if (isBidirectional)
		{
			final Pair<Link,Link> ee = getNetPlan().addLinkBidirectional(a.getNe(), b.getNe(), nominalLineRateGbps, -1, WNetConstants.WFIBER_DEFAULT_PROPAGATIONSPEEDKMPERSEC, null, getIpLayer().getNe());
			return new WIpLink(ee.getFirst());
		}
		else
		{
			final Link ee = getNetPlan().addLink(a.getNe(), b.getNe(), nominalLineRateGbps, -1, WNetConstants.WFIBER_DEFAULT_PROPAGATIONSPEEDKMPERSEC, null, getIpLayer().getNe());
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
			res.put(vnfTypeName, new WVnfType(vnfTypeName, Quadruple.of (maxInputTraffic_Gbps , Triple.of(numCpus, numRam, numHd) , isConstrained , nodeNames) , arbitraryParamString));
		}
		return res;
	}

	public void addOrUpdateVnfType (String vnfTypeName , WVnfType info)
	{
		final SortedMap<String , WVnfType> newInfo = this.getVnfTypesMap();
		newInfo.put(vnfTypeName, info);
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
			infoThisVnf.add(entry.getValue().isConstrained()? "1"  : "0");
			infoThisVnf.add(entry.getValue().getValidMetroNodesForInstantiation().stream().collect(Collectors.joining(WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER)));
			matrix.add(infoThisVnf);
		}
		np.setAttributeAsStringMatrix(ATTNAME_VNFTYPELIST, matrix);
	}
	
	public SortedSet<String> getVnfTypeNames () { return new TreeSet<> (getVnfTypesMap().keySet()); }

	static void ex (String s) { throw new Net2PlanException (s); } 

	
	
}
