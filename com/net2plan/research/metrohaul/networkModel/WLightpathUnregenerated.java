package com.net2plan.research.metrohaul.networkModel;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

import com.net2plan.interfaces.networkDesign.Route;

/** This class represents a unidirectional lightpath of a given line rate, being a main path or backup path of a lightpath request.
 * The lighptath is unregenerated, and has no wavelength conversion: the same set of optical slot are occupied in all the 
 * traversed fibers
 *
 */
public class WLightpathUnregenerated extends WAbstractNetworkElement
{
	private static final String ATTNAMECOMMONPREFIX = "UnregLp_";
	private static final String ATTNAMESUFFIX_OCCUPIEDSLOTIDS = "occupiedSlotIds";
	private static final String ATTNAMESUFFIX_MODULATIONID = "modulationId";

	private final Route r;

	WLightpathUnregenerated (Route r) { super (r); this.r = r; }

	public Route getNe () { return (Route) e; }
	
	
	/** The origin node of the lighptath, that must the origin node of the associated lightpath request
	 * @return
	 */
	public WNode getA () { return new WNode (r.getIngressNode()); }
	/** The destination node of the lighptath, that must the destination node of the associated lightpath request
	 * @return
	 */
	public WNode getB () { return new WNode (r.getEgressNode()); }
	/** Returns the associated lightpath request
	 * @return
	 */
	public WLightpathRequest getLightpathRequest () { return new WLightpathRequest(r.getDemand()); }

	/** Returns the user-defined identifier of the modulation associated to the transponder realizing this lightpath
	 * @return
	 */
	public String getModulationId () { return r.getAttribute(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_MODULATIONID, ""); }
	/** Sets the user-defined identifier of the modulation associated to the transponder realizing this lightpath
	 * @param modulationId
	 */
	public void setModulationId (String modulationId) { r.setAttribute(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_MODULATIONID, modulationId); }
	/** Indicates if this is a backup lightpath, of a main path in the same lightpath request
	 * @return
	 */
	public boolean isBackupLightpath () { return r.isBackupRoute(); }
	/** Indicates if this is a main lightpath that has a backup lightpath in the same lightpath request
	 * @return
	 */
	public boolean isMainLightpathWithBackupRoutes () { return r.hasBackupRoutes(); }

	/** Adds a backup lightpath to this lightpath
	 * @param backup
	 */
	public void addBackupLightpath (WLightpathUnregenerated backup) { r.addBackupRoute(backup.getNe ()); }
	
	/** Removes a backup lightpath 
	 * @param backup
	 */
	public void removeBackupLightpath (WLightpathUnregenerated backup) { r.removeBackupRoute(backup.getNe ()); }

	/** Sets this lightpath as a backup lightpath of other lightpath in this request 
	 * @param backup
	 */
	public void setAsBackupLightpath (WLightpathUnregenerated mainLp) { mainLp.addBackupLightpath(this); }
	
	/** Returns the sequence of fibers traversed by this lp
	 * @return
	 */
	public List<WFiber> getSeqFibers () { return r.getSeqLinks().stream().map(e->new WFiber(e)).collect(Collectors.toList()); }
	/** Changes the sequence of fibers traversed by this lightpath
	 * @param newSeqFibers
	 */
	public void setSeqFibers (List<WFiber> newSeqFibers) { r.setSeqLinks(newSeqFibers.stream().map(ee->ee.getNe()).collect(Collectors.toList())); }
	/** Returns the set of optical slots occupied in all the traversed fibers of this lightpath
	 * @return
	 */
	public SortedSet<Integer> getOpticalSlotIds () { SortedSet<Integer> res = getAttributeAsSortedSetIntegerOrDefault(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_OCCUPIEDSLOTIDS , null);if (res == null) throw new RuntimeException (); return res;  }
	/** Changes the set of optical slots occupied in all the traversed fibers of this lightpath
	 * @param slotsIds
	 */
	public void setOpticalSlotIds (SortedSet<Integer> slotsIds) { r.setAttributeAsNumberList(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_OCCUPIEDSLOTIDS, new ArrayList<> (slotsIds)); }
	/** Returns the length in km of this lighptath, as the sum of the lengths of the traversed fibers
	 * @return
	 */
	public double getLengthInKm () { return r.getLengthInKm(); }
	/** Returns the propagation delay in ms of this lighptath, as the sum of the propagation delays of the traversed fibers
	 * @return
	 */
	public double getPropagationDelayInMs () { return r.getPropagationDelayInMiliseconds(); }
	
	/** Indicates if this lightpath is up (does not traverse any failed fiber/node)
	 * @return
	 */
	public boolean isUp () { return !r.isDown(); }
	/** Returns the number of optical slots occupied by the lightpath 
	 * @return
	 */
	public double getNumberOccupiedSlotIds () { return getOpticalSlotIds().size(); } 
	
	/** Removes this lightpath
	 */
	public void remove () {  r.remove(); this.getLightpathRequest().internalUpdateOfRoutesCarriedTrafficFromFailureState(); }
	
}
