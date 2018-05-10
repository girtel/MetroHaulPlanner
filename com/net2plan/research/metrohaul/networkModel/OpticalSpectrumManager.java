package com.net2plan.research.metrohaul.networkModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.utils.Pair;

public class OpticalSpectrumManager
{
	final private SortedMap<WFiber,SortedMap<Integer,SortedSet<WLightpathUnregenerated>>> occupation_f_s_ll = new TreeMap<> ();
	final private SortedMap<WLightpathUnregenerated,SortedMap<WFiber,SortedSet<Integer>>> occupation_ll_f_s = new TreeMap<> ();

	private OpticalSpectrumManager () {}
	
	public static OpticalSpectrumManager createFromRegularLps (WNet net)
    {
		final OpticalSpectrumManager osm = new OpticalSpectrumManager();
		for (WLightpathUnregenerated lp : net.getLightpaths())
			osm.allocateOccupation(lp, lp.getSeqFibers(), lp.getOpticalSlotIds());
        return osm;
    }

    public SortedSet<Integer> getAvailableSlotIds (Collection<WFiber> wdmLinks)
    {
        if (wdmLinks.isEmpty()) throw new Net2PlanException ("No WDM links");
        final SortedSet<Integer> validSlotIds = this.getIdleOpticalSlotIds(wdmLinks.iterator().next());
        final Iterator<WFiber> itLink = wdmLinks.iterator();
        itLink.next();
        while (itLink.hasNext())
            validSlotIds.retainAll(this.getIdleOpticalSlotIds(itLink.next()));
        return validSlotIds;
    }
    
    public SortedMap<WFiber,SortedSet<Integer>> getOccupiedResources (WLightpathRequest lp)
    {
    	return this.occupation_ll_f_s.getOrDefault(lp, new TreeMap<> ());
    }

    public SortedMap<Integer,SortedSet<WLightpathUnregenerated>> getOccupiedResources (WFiber fiber)
    {
    	return this.occupation_f_s_ll.getOrDefault(fiber, new TreeMap<> ());
    }

    public SortedSet<Integer> getOccupiedOpticalSlotIds (WFiber fiber)
    {
    	return new TreeSet<> (this.occupation_f_s_ll.get(fiber).keySet());
    }

    public boolean isAllocatable (Collection<WFiber> wdmLinks , SortedSet<Integer> slotIds)
    {
        if (wdmLinks.size() != new HashSet<> (wdmLinks).size()) return false;
        for (WFiber e : wdmLinks)
            if (!this.isOpticalSlotIdsValidAndIdle(e , slotIds))
                return false;
        return true;
    }

    public boolean isAlreadyAccounted (WLightpathUnregenerated lp) { return this.occupation_ll_f_s.get(lp) != null; }

    public boolean allocateOccupation (WLightpathUnregenerated lp , Collection<WFiber> wdmLinks , SortedSet<Integer> slotIds)
    {
    	if (isAlreadyAccounted(lp)) throw new Net2PlanException ("The lightpath has been already accounted for");
    	if (wdmLinks.isEmpty() || slotIds.isEmpty()) return false;
    	boolean clashesWithPreviousAllocations = false;
    	for (WFiber fiber : wdmLinks)
    	{
    		SortedMap<Integer,SortedSet<WLightpathUnregenerated>> thisFiberInfo = this.occupation_f_s_ll.get(fiber);
    		if (fiber == null) { thisFiberInfo = new TreeMap<> (); this.occupation_f_s_ll.put(fiber, thisFiberInfo); }
    		for (int slotId : slotIds)
    		{
    			SortedSet<WLightpathUnregenerated> currentCollidingLps = thisFiberInfo.get(slotId);
    			if (currentCollidingLps == null) { currentCollidingLps = new TreeSet<> (); thisFiberInfo.put(slotId, currentCollidingLps); }
    			if (!currentCollidingLps.isEmpty()) clashesWithPreviousAllocations = true;
    			currentCollidingLps.add(lp);
    		}
    	}
    	this.occupation_ll_f_s.put(lp, wdmLinks.stream().collect(Collectors.toMap(e->e, e->new TreeSet<> (slotIds) , (a,b)->b , TreeMap::new)));
    	return clashesWithPreviousAllocations;
    }

    public void releaseOccupation (WLightpathUnregenerated lp)
    {
    	final SortedMap<WFiber,SortedSet<Integer>> occupiedResources = occupation_ll_f_s.get(lp);
    	if (occupiedResources == null) return;
    	occupation_ll_f_s.remove(lp);
    	
    	for (Entry<WFiber,SortedSet<Integer>> resource : occupiedResources.entrySet())
    	{
    		final WFiber fiber = resource.getKey();
    		final SortedSet<Integer> slotIds = resource.getValue();
    		SortedMap<Integer,SortedSet<WLightpathUnregenerated>> thisFiberInfo = this.occupation_f_s_ll.get(fiber);
    		assert fiber != null;
    		for (int slotId : slotIds)
    		{
    			final SortedSet<WLightpathUnregenerated> thisLpAndOthers = thisFiberInfo.get(slotId);
    			assert thisLpAndOthers != null;
    			assert thisLpAndOthers.contains(lp);
    			thisLpAndOthers.remove(lp);
    			if (thisLpAndOthers.isEmpty()) 
    			{
    				thisFiberInfo.remove(slotId);
    				if (thisFiberInfo.isEmpty()) this.occupation_f_s_ll.remove(fiber);
    			}
    		}
    	}
    }

    
    public Optional<Integer> spectrumAssignment_firstFit(Collection<WFiber> seqFibers, int numContiguousSlotsRequired , Optional<Integer> minimumInitialSlotId)
    {
        assert !seqFibers.isEmpty();
        assert numContiguousSlotsRequired > 0;
        SortedSet<Integer> intersectionValidSlots = getAvailableSlotIds(seqFibers);
        if (minimumInitialSlotId.isPresent())
            intersectionValidSlots = intersectionValidSlots.tailSet(minimumInitialSlotId.get());
        if (intersectionValidSlots.size() < numContiguousSlotsRequired) return Optional.empty();
        
        final LinkedList<Integer> rangeValid = new LinkedList<> ();
        for (int slotId : intersectionValidSlots)
        {
            if (!rangeValid.isEmpty())
                if (rangeValid.getLast() != slotId - 1)
                    rangeValid.clear();
            rangeValid.add(slotId);
            assert rangeValid.size() <= numContiguousSlotsRequired;
            if (rangeValid.size() == numContiguousSlotsRequired) return Optional.of(rangeValid.getFirst());
        }
        return Optional.empty();
    }

    public Optional<Pair<Integer,Integer>> spectrumAssignment_firstFitTwoRoutes(Collection<WFiber> seqFibers_1, Collection<WFiber> seqFibers_2 , int numContiguousSlotsRequired)
    {
        final boolean haveLinksInCommon = !Sets.intersection(new HashSet<>(seqFibers_1)  , new HashSet<>(seqFibers_2)).isEmpty();
        if (!haveLinksInCommon)
        {
            final Optional<Integer> firstRouteInitialSlot = spectrumAssignment_firstFit(seqFibers_1, numContiguousSlotsRequired, Optional.empty());
            if (!firstRouteInitialSlot.isPresent()) return Optional.empty();
            final Optional<Integer> secondRouteInitialSlot = spectrumAssignment_firstFit(seqFibers_2, numContiguousSlotsRequired, Optional.empty());
            if (!secondRouteInitialSlot.isPresent()) return Optional.empty();
            return Optional.of(Pair.of(firstRouteInitialSlot.get(), secondRouteInitialSlot.get()));
        }

        /* With links in common */
        final SortedSet<Integer> fistPathValidSlots = getAvailableSlotIds(seqFibers_1);
        final SortedSet<Integer> secondPathValidSlots = getAvailableSlotIds(seqFibers_2);
        for(int initialSlot_1 :  fistPathValidSlots)
        {
            if (!isValidOpticalSlotIdRange(fistPathValidSlots, initialSlot_1, numContiguousSlotsRequired)) continue;
            for(int initialSlot_2 :  secondPathValidSlots)
            {
                if (Math.abs(initialSlot_1 - initialSlot_2) < numContiguousSlotsRequired) continue;
                if (!isValidOpticalSlotIdRange(secondPathValidSlots, initialSlot_2, numContiguousSlotsRequired)) continue;
                return Optional.of(Pair.of(initialSlot_1, initialSlot_2));
            }           
        }
        return Optional.empty();
    }

    public List<List<WFiber>> getRegenerationPoints (List<WFiber> seqFibers, double maxRegeneratorDistanceInKm)
    {
        final List<List<WFiber>> res = new ArrayList<> ();
        res.add(new ArrayList<> ());

        double accumDistance = 0;
        for (WFiber fiber : seqFibers)
        {
            final double fiberLengthInKm = fiber.getLengthInKm();
            if (fiberLengthInKm > maxRegeneratorDistanceInKm)
                throw new Net2PlanException(String.format("Fiber %d is longer (%f km) than the maximum distance without regenerators (%f km)", fiber.getId(), fiberLengthInKm, maxRegeneratorDistanceInKm));
            accumDistance += fiberLengthInKm;
            if (accumDistance > maxRegeneratorDistanceInKm)
            {
                res.add(new ArrayList<> (Arrays.asList(fiber)));
                accumDistance = fiberLengthInKm;
            }
            else
                res.get(res.size()-1).add(fiber);
            
        }
        return res;
    }

    public String printReport ()
    {
        final StringBuffer st = new StringBuffer ();
        final String RETURN = System.getProperty("line.separator");
        
        for (WFiber e : occupation_f_s_ll.keySet().stream().sorted((e1,e2)->Integer.compare(occupation_f_s_ll.get(e2).size (), occupation_f_s_ll.get(e1).size ())).collect(Collectors.toList()))
        {
            final SortedMap<Integer,SortedSet<WLightpathUnregenerated>> occupThisLink = occupation_f_s_ll.get(e);
            final int numOchSubpaths = occupThisLink.values().stream().flatMap(s->s.stream()).collect(Collectors.toSet()).size();
            final int numOccupSlots = occupThisLink.size();
            final boolean hasClashing = occupThisLink.values().stream().anyMatch(s->s.size() > 1);
            st.append("Link " + e + ". Occup slots: " + numOccupSlots + ", cap: " + e.getNumberOfOpticalChannelsPerFiber() + ", num Och subpaths: " + numOchSubpaths + ", clashing: " + hasClashing + RETURN);
        }
        return st.toString();
    }

    public void checkNetworkSlotOccupation ()
    {
        for (Entry<WFiber,SortedMap<Integer,SortedSet<WLightpathUnregenerated>>> occup_e : occupation_f_s_ll.entrySet())
        {
            final WFiber e = occup_e.getKey();
            assert e.isBidirectional();
            final SortedMap<Integer,SortedSet<WLightpathUnregenerated>> occup = occup_e.getValue();
            if (!e.getValidOpticalSlotIds().containsAll(occup.keySet())) throw new Net2PlanException ("The optical slots occupied at link " + e + " are outside the valid range");
            for (Set<WLightpathUnregenerated> rs : occup.values())
                if (rs.size() != 1) throw new Net2PlanException ("The optical slots occupation is not correct");
        }       
    }
    

    
    private static boolean isValidOpticalSlotIdRange (SortedSet<Integer> validSlots , int initialSlot , int numContiguousSlots)
    {
        for (int cont = 0; cont < numContiguousSlots ; cont ++)
            if (!validSlots.contains(initialSlot + cont)) return false;
        return true;
    }

	public SortedSet<Integer> getIdleOpticalSlotIds (WFiber wdmLink)
	{
		final SortedSet<Integer> res = wdmLink.getValidOpticalSlotIds();
		res.removeAll(getOccupiedOpticalSlotIds(wdmLink));
		return res;
	}
	
	public boolean isOpticalSlotIdsValidAndIdle (WFiber wdmLink , SortedSet<Integer> slotsIds)
	{
		return getIdleOpticalSlotIds(wdmLink).containsAll(slotsIds);
	}
	


	
	
}
