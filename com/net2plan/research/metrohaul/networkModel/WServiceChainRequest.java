package com.net2plan.research.metrohaul.networkModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.net2plan.interfaces.networkDesign.Demand;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.utils.Pair;

public class WServiceChainRequest extends WAbstractNetworkElement
{
	private static final String ATTNAMECOMMONPREFIX = "ServiceChainRequest_";
	private static final String ATTNAMESUFFIX_TIMESLOTANDINTENSITYINGBPS = "timeSlotAndIntensityInGbps";
	private static final String ATTNAMESUFFIX_USERSERVICENAME = "userServiceName";
	private static final String ATTNAMESUFFIX_ISUPSTREAM = "isUpstream";
	private static final String ATTNAMESUFFIX_VALIDINPUTNODENAMES = "validInputNodeNames";
	private static final String ATTNAMESUFFIX_VALIDOUTPUTNODENAMES = "validOutputNodeNames";
	private static final String ATTNAMESUFFIX_SEQUENCEOFEXPANSIONFACTORRESPECTTOINJECTION = "sequenceOfPerVnfExpansionFactorsRespectToInjection";
	
	private final Demand sc; // from anycastIN to anycastOUT
	
	public WServiceChainRequest(Demand sc) 
	{ 
		super(sc); 
		this.sc = sc; 
		assert sc.getLayer().getIndex() == 1;
		assert new WNode (sc.getIngressNode()).isVirtualNode();
		assert new WNode (sc.getEgressNode()).isVirtualNode();
	}

	public int getNumberVnfsToTraverse () { return sc.getServiceChainSequenceOfTraversedResourceTypes().size(); }
	
	public List<Double> getSequenceOfExpansionFactorsRespectToInjection () 
	{
		final int numVnfs = getNumberVnfsToTraverse();
		if (numVnfs == 0) return Arrays.asList();
		final Double [] defaultVal = new Double [getNumberVnfsToTraverse()]; Arrays.fill(defaultVal, 1.0);
		return getAttributeAsListDoubleOrDefault(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_SEQUENCEOFEXPANSIONFACTORRESPECTTOINJECTION, Arrays.asList(defaultVal));
	}

	public WServiceChain addServiceChain (List<? extends WAbstractNetworkElement> sequenceOfIpLinksAndResources , double injectionTrafficGbps)
	{
		return null;
	}
	
	public void setSequenceOfExpansionFactorsRespectToInjection (List<Double>  sequenceOfExpansionFactors) 
	{
		final int numVnfs = getNumberVnfsToTraverse();
		if (sequenceOfExpansionFactors.size() != numVnfs) throw new Net2PlanException ("Wrong number of expansion factors");
		getNe().setAttributeAsNumberList(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_SEQUENCEOFEXPANSIONFACTORRESPECTTOINJECTION, (List<Number>) (List<?>) sequenceOfExpansionFactors);
	}
	
	public SortedSet<WNode> getPotentiallyValidOrigins () 
	{ 
		final List<String> resNames = sc.getAttributeAsStringList(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_VALIDINPUTNODENAMES , null);
		if (resNames == null) throw new Net2PlanException ("The list of initial nodes is empty");
		final SortedSet<WNode> res = new TreeSet<> ();
		for (String name : resNames) { final WNode nn = this.getNet().getNodeByName(name).orElse(null); if (nn != null) res.add(nn); } 
		if (res.isEmpty()) throw new Net2PlanException ("The list of initial nodes is empty");
		return res;
	}

	public SortedSet<WNode> getPotentiallyValidDestinations () 
	{ 
		final List<String> resNames = sc.getAttributeAsStringList(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_VALIDOUTPUTNODENAMES , null);
		if (resNames == null) throw new Net2PlanException ("The list of initial nodes is empty");
		final SortedSet<WNode> res = new TreeSet<> ();
		for (String name : resNames) { final WNode nn = this.getNet().getNodeByName(name).orElse(null); if (nn != null) res.add(nn); } 
		if (res.isEmpty()) throw new Net2PlanException ("The list of initial nodes is empty");
		return res;
	}

	public Optional<Double> getTrafficIntensityInfoOrDefault (String timeSlotName)
	{
		return getFullTrafficIntensityInfo().stream().filter(p->p.getFirst().equals(timeSlotName)).map(p->p.getSecond()).findFirst();  
	}
	
	public List<String> getSequenceVnfTypes () { return sc.getServiceChainSequenceOfTraversedResourceTypes(); }
	
	public String getUserServiceName () 
	{ 
		final String res = getAttributeOrDefault(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_USERSERVICENAME , null);
		assert res != null; 
		return res;
	}

	public List<Pair<String,Double>> getFullTrafficIntensityInfo ()
	{
		final List<String> vals = sc.getAttributeAsStringList(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_TIMESLOTANDINTENSITYINGBPS, null);
		if (vals == null) return new ArrayList<> ();
		try
		{
			final Set<String> previousTimeSlotIds = new HashSet<> ();
			final List<Pair<String,Double>> res = new ArrayList<> ();
			final Iterator<String> it = vals.iterator();
			while (it.hasNext())
			{
				final String timeSlotName = it.next();
				final Double intensity = Double.parseDouble(it.next());
				if (previousTimeSlotIds.contains(timeSlotName)) continue; else previousTimeSlotIds.add(timeSlotName);
				res.add(Pair.of(timeSlotName, intensity));
			}
			return res;
		} catch (Exception e) { return new ArrayList<> (); }
	}
	public void setFullTrafficIntensityInfo (List<Pair<String,Double>> info)
	{
		final List<String> vals = new ArrayList<> ();
		final Set<String> previousTimeSlotIds = new HashSet<> ();
		for (Pair<String,Double> p : info)
		{
			final String timeSlotName = p.getFirst();
			final Double intensity = p.getSecond();
			if (previousTimeSlotIds.contains(timeSlotName)) continue; else previousTimeSlotIds.add(timeSlotName);
			vals.add(timeSlotName); vals.add(intensity.toString());
		}
		sc.setAttributeAsStringList(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_TIMESLOTANDINTENSITYINGBPS, vals);
	}
	
	public double getCurrentOfferedTrafficInGbps () { return sc.getOfferedTraffic(); }
	
	public void setCurrentOfferedTrafficInGbps (double offeredTrafficGbps) { sc.setOfferedTraffic(offeredTrafficGbps); }

	public boolean isUpstream () { final Boolean res = getAttributeAsBooleanOrDefault(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_ISUPSTREAM, null); assert res != null; return res; }
	
	@Override
	public Demand getNe() { return (Demand) e; }

}
