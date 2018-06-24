package com.net2plan.research.metrohaul.networkModel;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

import com.net2plan.interfaces.networkDesign.Route;

public class WLightpathUnregenerated extends WAbstractNetworkElement
{
	private static final String ATTNAMECOMMONPREFIX = "UnregLp_";
	private static final String ATTNAMESUFFIX_OCCUPIEDSLOTIDS = "occupiedSlotIds";
	private static final String ATTNAMESUFFIX_MODULATIONID = "modulationId";

	private final Route r;

	WLightpathUnregenerated (Route r) { super (r); this.r = r; }

	public Route getNe () { return (Route) e; }
	public WNode getA () { return new WNode (r.getIngressNode()); }
	public WNode getB () { return new WNode (r.getEgressNode()); }
	public WLightpathRequest getLightpathRequest () { return new WLightpathRequest(r.getDemand()); }

	public String getModulationId () { return r.getAttribute(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_MODULATIONID, ""); }
	public void setModulationId (String modulationId) { r.setAttribute(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_MODULATIONID, modulationId); }
	public boolean isBackupLightpath () { return r.isBackupRoute(); }
	public boolean isMainLightpathWithBackupRoutes () { return r.hasBackupRoutes(); }
	public void addBackupLightpath (WLightpathUnregenerated backup) { r.addBackupRoute(backup.getNe ()); }
	public void removeBackupLightpath (WLightpathUnregenerated backup) { r.removeBackupRoute(backup.getNe ()); }
	public void setAsBackupLightpath (WLightpathUnregenerated mainLp) { mainLp.addBackupLightpath(this); }
	
	public List<WFiber> getSeqFibers () { return r.getSeqLinks().stream().map(e->new WFiber(e)).collect(Collectors.toList()); }
	public void setSeqFibers (List<WFiber> newSeqFibers) { r.setSeqLinks(newSeqFibers.stream().map(ee->ee.getNe()).collect(Collectors.toList())); }
	public SortedSet<Integer> getOpticalSlotIds () { SortedSet<Integer> res = getAttributeAsSortedSetIntegerOrDefault(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_OCCUPIEDSLOTIDS , null);if (res == null) throw new RuntimeException (); return res;  }
	public void setOpticalSlotIds (SortedSet<Integer> slotsIds) { r.setAttributeAsNumberList(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_OCCUPIEDSLOTIDS, new ArrayList<> (slotsIds)); }
	public double getLengthInKm () { return r.getLengthInKm(); }
	public double getPropagationDelayInMs () { return r.getPropagationDelayInMiliseconds(); }
	
	public boolean isUp () { return !r.isDown(); }
	public double getNumberOccupiedSlotIds () { return getOpticalSlotIds().size(); } 
	
	public void remove () {  r.remove(); this.getLightpathRequest().internalUpdateOfRoutesCarriedTrafficFromFailureState(); }
	
}
