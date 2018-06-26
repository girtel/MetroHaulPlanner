package com.net2plan.research.metrohaul.networkModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetworkElement;
import com.net2plan.interfaces.networkDesign.Resource;
import com.net2plan.interfaces.networkDesign.Route;
import com.net2plan.utils.Pair;

/** Instances of this class are service chains, realizing service chain requests. A service chain should start in one of the origin nodes of the service chain, and end in 
 * one of the destination nodes of the service chain. The injection traffic of the service chain is the traffic produced by its origin node. Note that when the service chain 
 * traffic traverses a VNF, its volume will increase or decrease according to the expansion factor defined for that VNF.
 * 
 *  
 *
 */
public class WServiceChain extends WAbstractNetworkElement
{
	WServiceChain(Route r) 
	{ 
		super (r); 
		this.r = r; 
		assert r.getLayer().getIndex() == 1; 
	}

	final private Route r;

	@Override
	public Route getNe() { return r; }

	/** Returns the service chain request that this service chain is realizing
	 * @return
	 */
	public WServiceChainRequest getServiceChainRequest () { return new WServiceChainRequest(r.getDemand()); }
	
	/** Returns the sequence of traversed IP links, filtering out any VNF instance traversed
	 * @return
	 */
	public List<WIpLink> getSequenceOfTraversedIpLinks () 
	{
		return r.getSeqLinks().stream().map(ee->new WIpLink (ee)).collect(Collectors.toCollection(ArrayList::new));
	}
	/** Returns the sequence of traversed VNF instances, filtering out any IP link traversed
	 * @return
	 */
	public List<WVnfInstance> getSequenceOfTraversedVnfInstances () 
	{
		return r.getSeqResourcesTraversed().stream().map(ee->new WVnfInstance (ee)).collect(Collectors.toCollection(ArrayList::new));
	}
	/** Returns the sequence of traversed IP links and VNF instances traversed
	 * @return
	 */
	public List<? extends WAbstractNetworkElement> getSequenceOfLinksAndVnfs ()
	{
		return r.getPath().stream().map(ee->(WAbstractNetworkElement) (ee instanceof Link? new WIpLink ((Link) ee) : new WVnfInstance ((Resource) ee))).collect(Collectors.toCollection(ArrayList::new));
	}
	/** Returns the traffic injected by the service chain origin node in Gbps
	 * @return
	 */
	public double getInitiallyInjectedTrafficGbps () { return r.getCarriedTraffic(); }

	/** Sets the traffic injected by the service chain origin node in Gbps
	 * @param injectedTrafficGbps
	 */
	public void setInitiallyInjectedTrafficGbps (double injectedTrafficGbps) { setPathAndInitiallyInjectedTraffic(Optional.of(injectedTrafficGbps), Optional.empty()); }

	/** Reconfigures the path of the service chain: the sequence of IP links and VNF instances to traverse
	 * @param seqIpLinksAndVnfInstances
	 */
	public void setPath (List<? extends WAbstractNetworkElement> seqIpLinksAndVnfInstances) { setPathAndInitiallyInjectedTraffic(Optional.empty() , Optional.of(seqIpLinksAndVnfInstances)); }

	/** Reconfigures in one shot: (i) the injected traffic by the origin node of the service chain, and (ii) the path of the chain: the sequence of traversed IP links and VNF instances
	 * @param optInjectedTrafficGbps
	 * @param optSeqIpLinksAndVnfInstances
	 */
	public void setPathAndInitiallyInjectedTraffic (Optional<Double> optInjectedTrafficGbps , Optional<List<? extends WAbstractNetworkElement>> optSeqIpLinksAndVnfInstances)
	{
		final double injectedTrafficGbps = optInjectedTrafficGbps.orElse(this.getInitiallyInjectedTrafficGbps());
		final List<? extends WAbstractNetworkElement> seqIpLinksAndVnfInstances = optSeqIpLinksAndVnfInstances.orElse(this.getSequenceOfLinksAndVnfs());
		final Pair<List<NetworkElement> , List<Double>> npInfo = computeScPathAndOccupationInformationAndCheckValidity (this.getServiceChainRequest() , injectedTrafficGbps , seqIpLinksAndVnfInstances);
		r.setPath(injectedTrafficGbps, npInfo.getFirst(), npInfo.getSecond());
	}
	
	static Pair<List<NetworkElement> , List<Double>> computeScPathAndOccupationInformationAndCheckValidity (WServiceChainRequest scRequest , double injectedTrafficGbps , List<? extends WAbstractNetworkElement> seqIpLinksAndVnfInstances)
	{
		// checks
		if (seqIpLinksAndVnfInstances.isEmpty()) throw new Net2PlanException ("Wrong path");
		if (seqIpLinksAndVnfInstances.stream().anyMatch(e->!e.isVnfInstance() && !e.isWIpLink())) throw new Net2PlanException ("Wrong path");
		final List<WIpLink> seqIpLinks = seqIpLinksAndVnfInstances.stream().filter(e->e.isWIpLink()).map(ee->(WIpLink) ee).collect(Collectors.toList());
		final WNode firstNode = seqIpLinks.get(0).getA();
		final WNode lastNode = seqIpLinks.get(seqIpLinks.size()-1).getB();
		if (!scRequest.getPotentiallyValidOrigins().isEmpty()) if (scRequest.getPotentiallyValidOrigins().contains(firstNode)) throw new Net2PlanException ("Wrong path"); 
		if (!scRequest.getPotentiallyValidDestinations().isEmpty()) if (scRequest.getPotentiallyValidDestinations().contains(lastNode)) throw new Net2PlanException ("Wrong path");
		final List<Double> newOccupationInformation = new ArrayList<> ();
		int numVnfsAlreadyTraversed = 0;
		double currentInjectedTrafficGbps = injectedTrafficGbps;
		final List<Double> expensionFactorsRespectToInitialUserTrafficAfterTravVnf = scRequest.getSequenceOfExpansionFactorsRespectToInjection();
		List<NetworkElement> newPathNp = new ArrayList<> ();
		newPathNp.add(firstNode.getIncomingLinkFromAnycastOrigin());
		newOccupationInformation.add(injectedTrafficGbps);
		for (WAbstractNetworkElement element : seqIpLinksAndVnfInstances)
		{
			newPathNp.add (element.getNe());
			newOccupationInformation.add(currentInjectedTrafficGbps);
			if (element.isVnfInstance())
			{
				currentInjectedTrafficGbps = injectedTrafficGbps * expensionFactorsRespectToInitialUserTrafficAfterTravVnf.get(numVnfsAlreadyTraversed); 
				numVnfsAlreadyTraversed ++;
			}
		}
		newPathNp.add(lastNode.getOutgoingLinkToAnycastDestination());
		newOccupationInformation.add(currentInjectedTrafficGbps);
		return Pair.of(newPathNp, newOccupationInformation);
	}
	
	
	
}
