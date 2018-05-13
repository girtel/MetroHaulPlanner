
//CONTINUACION:
//	1. ADD SERVICE CHAIN 
//	2. IMPORTAR DESDE EXCEL --> CON JOSE LUIS
//	2. HACER GETKSHORTESTPATH PARA UN SERVICE CHAIN REQUEST DADA
//	4. METER ALGORITMO GENERICO NO FILTERLESS

package com.net2plan.research.metrohaul.networkModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.net2plan.interfaces.networkDesign.Demand;
import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.utils.Pair;

public class WNet extends WAbstractNetworkElement
{
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
		if (name.contains(WNetConstants.WNODE_NODENAMEINVALIDCHARACTER)) throw new Net2PlanException("Names cannot contain the character: " + WNetConstants.WNODE_NODENAMEINVALIDCHARACTER);  
		if (getNodes().stream().anyMatch(n->n.getName().equals(name))) ex("Names cannot be repeated");
		final WNode n = new WNode (getNetPlan().addNode(xCoord, yCoord, name, null));
		n.setType(type);
		addIpLink(getAnycastOriginNode(), n, Double.MAX_VALUE, false);
		addIpLink(n , getAnycastDestinationNode(), Double.MAX_VALUE, false);
		return n;
	}
	
	public WFiber addFiber (WNode a , WNode b , List<Integer> validOpticalSlotRanges , double lengthInKm , boolean isBidirectional)
	{
		final SortedSet<Integer> opticalSlots = WFiber.computeValidOpticalSlotIds(validOpticalSlotRanges);
		if (isBidirectional)
		{
			final Pair<Link,Link> ee = getNetPlan().addLinkBidirectional(a.getNe(), b.getNe(), opticalSlots.size(), lengthInKm, WNetConstants.WFIBER_DEFAULT_PROPAGATIONSPEEDKMPERSEC, null, getWdmLayer().getNe());
			return new WFiber(ee.getFirst());
		}
		else
		{
			final Link ee = getNetPlan().addLink(a.getNe(), b.getNe(), opticalSlots.size(), lengthInKm, WNetConstants.WFIBER_DEFAULT_PROPAGATIONSPEEDKMPERSEC, null, getWdmLayer().getNe());
			return new WFiber(ee);
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
	
	static void ex (String s) { throw new Net2PlanException (s); } 
	
}
