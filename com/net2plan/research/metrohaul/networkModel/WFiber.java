package com.net2plan.research.metrohaul.networkModel;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.Net2PlanException;

public class WFiber extends WAbstractNetworkElement
{
	private static final String ATTNAMECOMMONPREFIX = "Fiber_";
	private static final String ATTNAMESUFFIX_ATTENUATIONCOEFFICIENTDBPERKM = "FiberAttenuationCoefficient_dbPerKm";
	private static final String ATTNAMESUFFIX_AMPLIFIERPOSITIONFROMORIGINNODE_KM = "AmplifierPositionsFromOriginNode_km";
	private static final String ATTNAMESUFFIX_AMPLIFIERGAINS_DB = "AmplifierGains_dB";
	private static final String ATTNAMESUFFIX_VALIDOPTICALSLOTRANGES = "OpticalSlotRanges";

	public Link getNe() { return (Link) e; }
	public WFiber (Link e) { super (e); }
	public WNode getA () { return new WNode (getNe().getOriginNode()); }
	public WNode getB () { return new WNode (getNe().getDestinationNode()); }
	public double getLengthInKm () { return getNe().getLengthInKm(); }
	public void setLenghtInKm (double lenghtInKm) { getNe().setLengthInKm(lenghtInKm); }
	public boolean isBidirectional () { return getNe().isBidirectional(); }
	public double getAttenuationCoefficient_dbPerKm () { return getAttributeAsDoubleOrFail(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_ATTENUATIONCOEFFICIENTDBPERKM); } 
	public void setAttenuationCoefficient_dbPerKm (double attenuationCoef_dbPerKm) { getNe().setAttribute(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_ATTENUATIONCOEFFICIENTDBPERKM , new Double (attenuationCoef_dbPerKm).toString()); }
	public WFiber getBidirectionalPair () { if (!this.isBidirectional()) throw new Net2PlanException ("Not a bidirectional link"); return new WFiber (getNe().getBidirectionalPair()); }
	public List<Double> getAmplifierPositionsKmFromOrigin_km () { return getAttributeAsListDoubleOrFail (ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_AMPLIFIERPOSITIONFROMORIGINNODE_KM); }
	public void setAmplifierPositionsKmFromOrigin_km (List<Double> positions) 
	{
		if (positions.stream().anyMatch(p->p<0 || p>getLengthInKm())) throw new Net2PlanException ("Amplifier outside of position");
		getNe().setAttributeAsNumberList(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_AMPLIFIERPOSITIONFROMORIGINNODE_KM , (List<Number>) (List<?>) positions); 
	}
	public List<Double> getAmplifierGains_dB () { return getAttributeAsListDoubleOrFail (ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_AMPLIFIERGAINS_DB); }
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
        return getAttributeAsListDoubleOrFail (ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_VALIDOPTICALSLOTRANGES).stream().map(ee->ee.intValue()).collect(Collectors.toList());
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
}
