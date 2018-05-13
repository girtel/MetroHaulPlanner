package com.net2plan.research.metrohaul.networkModel;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.net2plan.interfaces.networkDesign.Demand;
import com.net2plan.interfaces.networkDesign.Net2PlanException;

public class WLightpathRequest extends WAbstractNetworkElement
{
	final private Demand d;
	private static final String ATTNAMECOMMONPREFIX = "LightpathRequest_";
	private static final String ATTNAMESUFFIX_ISTOBE11PROTECTED = "isToBe11Protected";

	
	WLightpathRequest (Demand d) 
	{ 
		super (d); 
		this.d = d; 
		assert d.getRoutes().size() <= 2; 
		if (d.getRoutes().size() == 2) assert d.getRoutesAreBackup().size() == 1; 
		assert !getA().isVirtualNode() && !getB().isVirtualNode();  
	}
	
	
	
	public Demand getNe () { return (Demand) e; }
	public WNode getA () { return new WNode (getNe().getIngressNode()); }
	public WNode getB () { return new WNode (getNe().getEgressNode()); }
	public boolean isBidirectional () { return getNe().isBidirectional(); }
	public WLightpathRequest getBidirectionalPair () { assert this.isBidirectional(); return new WLightpathRequest(d.getBidirectionalPair()); }
	public List<WLightpathUnregenerated> getLightpaths () { return getNe().getRoutes().stream().map(r->new WLightpathUnregenerated(r)).collect(Collectors.toList()); }
	public double getLineRateGbps () { return d.getOfferedTraffic(); }
	public void setLineRateGbps (double lineRateGbps) { d.setOfferedTraffic(lineRateGbps); }
	public boolean is11Protected () { return !d.getRoutesAreBackup().isEmpty(); }
	public boolean isToBe11Protected () { return getAttributeAsBooleanOrDefault(ATTNAMECOMMONPREFIX +ATTNAMESUFFIX_ISTOBE11PROTECTED , WNetConstants.WLPREQUEST_DEFAULT_ISTOBE11PROTECTED); }
	public void setIsToBe11Protected (boolean isToBe11Protected) { d.setAttribute(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_ISTOBE11PROTECTED , new Boolean (isToBe11Protected).toString()); }
	public double getWorstCaseLengthInKm () 
	{
		return getLightpaths().stream().mapToDouble(lp->lp.getLengthInKm()).max().orElse(Double.MAX_VALUE);
	}
	public double getWorstCasePropagationDelayMs () 
	{
		return getLightpaths().stream().mapToDouble(lp->lp.getPropagationDelayInMs()).max().orElse(Double.MAX_VALUE);
	}
	
	
	public boolean hasRoutes () { return !d.getRoutes().isEmpty(); }
	public boolean isBlocked () 
	{
		if (!hasRoutes()) return true;
		return !getLightpaths().stream().anyMatch(lp->lp.isUp());
	}
	public double getCarriedTrafficGbps ()
	{
		return isBlocked()? 0.0 : getLineRateGbps();
	}

	public void remove () { d.remove(); }

	
	public WLightpathUnregenerated addLightpathUnregenerated (List<WFiber> sequenceFibers , 
			SortedSet<Integer> occupiedSlots ,
			Optional<List<WFiber>> oppositeSeqFibersIfBidirectional , 
			Optional<SortedSet<Integer>> oppositeOccupiedSlotsIfBidirectional ,  
			boolean isBackupRoute)
	{
		final int numRoutesAlready = this.getLightpaths().size();
		if (numRoutesAlready == 2) throw new Net2PlanException ("Already two lightpaths");
		if (numRoutesAlready == 1 && !this.isToBe11Protected()) throw new Net2PlanException ("Already one lightpath");
		if (numRoutesAlready == 1 && !isBackupRoute) throw new Net2PlanException ("Not a backup lightpath");
		if (numRoutesAlready == 0 && isBackupRoute) throw new Net2PlanException ("Not a main lightpath yet");
		final WLightpathUnregenerated lp12 = new WLightpathUnregenerated(getNetPlan().addRoute(getNe(), 
				this.getLineRateGbps(), 
				occupiedSlots.size(), sequenceFibers.stream().map(ee->ee.getNe()).collect(Collectors.toList()), 
				null));
		lp12.setOpticalSlotIds(occupiedSlots);
		if (isBackupRoute) lp12.setAsBackupLightpath(getLightpaths().get(0));
		this.internalUpdateOfRoutesCarriedTrafficFromFailureState();
		return lp12;
	}
	
	public void coupleToIpLink (WIpLink ipLink) { ipLink.coupleToLightpathRequest(this); }
	
	public void decouple () { if (!d.isCoupled()) return; d.decouple(); }
	
	public boolean isCoupledToIpLink () { return d.isCoupled(); } 
	
	public WIpLink getCoupledIpLink () { if (!isCoupledToIpLink()) throw new Net2PlanException ("Not coupled"); return new WIpLink(d.getCoupledLink()); }

	
	void internalUpdateOfRoutesCarriedTrafficFromFailureState ()
	{
		if (!hasRoutes()) return;
		if (!is11Protected())
		{
			final WLightpathUnregenerated lp = getLightpaths().get(0);
			lp.getNe().setCarriedTraffic(lp.isUp()? getLineRateGbps() : 0.0 , null);
		}
		else
		{
			final List<WLightpathUnregenerated> lps = getLightpaths();
			if (lps.get(0).isUp()) 
			{ 
				lps.get(0).getNe().setCarriedTraffic(getLineRateGbps(), null);   
				lps.get(1).getNe().setCarriedTraffic(0.0, null);   
			}
			else
			{
				lps.get(0).getNe().setCarriedTraffic(0.0 , null);   
				lps.get(1).getNe().setCarriedTraffic(lps.get(1).isUp()? getLineRateGbps() : 0.0 , null);   
			}
		}
	}
	
}
