package com.net2plan.research.metrohaul.networkModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.utils.Pair;

public class WFiber extends WAbstractNetworkElement
{
	private static final String ATTNAMECOMMONPREFIX = "Fiber_";
	private static final String ATTNAMESUFFIX_ATTENUATIONCOEFFICIENTDBPERKM = "FiberAttenuationCoefficient_dbPerKm";
	private static final String ATTNAMESUFFIX_AMPLIFIERPOSITIONFROMORIGINNODE_KM = "AmplifierPositionsFromOriginNode_km";
	private static final String ATTNAMESUFFIX_AMPLIFIERGAINS_DB = "AmplifierGains_dB";
	private static final String ATTNAMESUFFIX_VALIDOPTICALSLOTRANGES = "OpticalSlotRanges";
	private final Link e;
	
	public Link getNe() { return (Link) e; }
	public WFiber (Link e) { super (e); this.e = e; }
	
	public WNode getA () { return new WNode (e.getOriginNode()); }
	public WNode getB () { return new WNode (e.getDestinationNode()); }
	public double getLengthInKm () { return e.getLengthInKm(); }
	public void setLenghtInKm (double lenghtInKm) { e.setLengthInKm(lenghtInKm); }
	public boolean isBidirectional () { return e.isBidirectional(); }
	public double getAttenuationCoefficient_dbPerKm () { return getAttributeAsDoubleOrDefault(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_ATTENUATIONCOEFFICIENTDBPERKM , WNetConstants.WFIBER_DEFAULT_ATTCOEFFICIENTDBPERKM); } 
	public void setAttenuationCoefficient_dbPerKm (double attenuationCoef_dbPerKm) { e.setAttribute(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_ATTENUATIONCOEFFICIENTDBPERKM , new Double (attenuationCoef_dbPerKm).toString()); }
	public WFiber getBidirectionalPair () { if (!this.isBidirectional()) throw new Net2PlanException ("Not a bidirectional link"); return new WFiber (e.getBidirectionalPair()); }
	public List<Double> getAmplifierPositionsKmFromOrigin_km () { return getAttributeAsListDoubleOrDefault (ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_AMPLIFIERPOSITIONFROMORIGINNODE_KM , new ArrayList<> ()); }
	public void setAmplifierPositionsKmFromOrigin_km (List<Double> positions) 
	{
		if (positions.stream().anyMatch(p->p<0 || p>getLengthInKm())) throw new Net2PlanException ("Amplifier outside of position");
		e.setAttributeAsNumberList(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_AMPLIFIERPOSITIONFROMORIGINNODE_KM , (List<Number>) (List<?>) positions); 
	}
	public List<Double> getAmplifierGains_dB () { return getAttributeAsListDoubleOrDefault (ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_AMPLIFIERGAINS_DB , new ArrayList<>()); }
	public void setAmplifierGains_dB (List<Double> gains_db) 
	{
		e.setAttributeAsNumberList(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_AMPLIFIERGAINS_DB , (List<Number>) (List<?>) gains_db); 
	}
    public final void setValidOpticalSlotRanges (List<Integer> listInitialEndSlotRanges)
    {
        final SortedSet<Integer> cache_validSlotsIds = computeValidOpticalSlotIds(listInitialEndSlotRanges);
        e.setAttributeAsNumberList(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_VALIDOPTICALSLOTRANGES, (List<Number>) (List<?>) listInitialEndSlotRanges);
    }
    public final List<Integer> getValidOpticalSlotRanges ()
    {
        return getAttributeAsListDoubleOrDefault (ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_VALIDOPTICALSLOTRANGES , WNetConstants.WFIBER_DEFAULT_VALIDOPTICALSLOTRANGES).stream().map(ee->ee.intValue()).collect(Collectors.toList());
    }
    public final SortedSet<Integer> getValidOpticalSlotIds ()
    {
        return computeValidOpticalSlotIds(getValidOpticalSlotRanges());
    }
	static final SortedSet<Integer> computeValidOpticalSlotIds (List<Integer> validOpticalSlotRanges) 
    { 
        final SortedSet<Integer> res = new TreeSet<> ();
        final Iterator<Integer> it = validOpticalSlotRanges.iterator();
        while (it.hasNext())
        {
            final int startRange = it.next();
            if (!it.hasNext()) throw new Net2PlanException ("Invalid optical slot ranges");
            final int endRange = it.next();
            if (endRange < startRange) throw new Net2PlanException ("Invalid optical slot ranges");
            for (int i = startRange ; i <= endRange ; i ++) res.add(i);
        }
        return res; 
    }

	public SortedSet<WLightpathUnregenerated> getTraversingLps () { return e.getTraversingRoutes().stream().map(r->new WLightpathUnregenerated(r)).collect(Collectors.toCollection(TreeSet::new)); }

	public SortedSet<WLightpathRequest> getTraversingLpRequestsInAtLeastOneLp () { return e.getTraversingRoutes().stream().map(r->new WLightpathRequest(r.getDemand())).collect(Collectors.toCollection(TreeSet::new)); }
	
	public boolean isUp () { return e.isUp(); }
	
	public void setAsUp () 
	{ 
		e.setFailureState(true);
		for (WLightpathRequest lpReq : this.getTraversingLpRequestsInAtLeastOneLp())
			lpReq.internalUpdateOfRoutesCarriedTrafficFromFailureState();
	}
	public void setAsDown () 
	{ 
		e.setFailureState(false);
		for (WLightpathRequest lpReq : this.getTraversingLpRequestsInAtLeastOneLp())
			lpReq.internalUpdateOfRoutesCarriedTrafficFromFailureState();
	}

	public void remove () { this.setAsDown(); e.remove(); }

	public Pair<Integer,Integer> getMinMaxValidSlotId ()
	{
		final SortedSet<Integer> res = this.getValidOpticalSlotIds();
		return Pair.of(res.first(), res.last());
	}

	public SortedSet<Integer> getIdleOpticalSlotIds ()
	{
		final SortedSet<Integer> res = getValidOpticalSlotIds();
		for (WLightpathUnregenerated lp : getTraversingLps())
			res.removeAll(lp.getOpticalSlotIds());
		return res;
	}

	public boolean isOpticalSlotIdsValidAndIdle (SortedSet<Integer> slotsIds)
	{
		return getIdleOpticalSlotIds().containsAll(slotsIds);
	}
	
	public int getNumberOfOpticalChannelsPerFiber () { return getValidOpticalSlotIds().size(); }
}
