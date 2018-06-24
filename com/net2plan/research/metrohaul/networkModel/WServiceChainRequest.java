package com.net2plan.research.metrohaul.networkModel;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.net2plan.interfaces.networkDesign.Demand;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetworkElement;
import com.net2plan.interfaces.networkDesign.Route;
import com.net2plan.utils.Pair;

public class WServiceChainRequest extends WAbstractNetworkElement
{
	private static final String ATTNAMECOMMONPREFIX = "ServiceChainRequest_";
	private static final String ATTNAMESUFFIX_TIMESLOTANDINTENSITYINGBPS = "timeSlotAndInitialInjectionIntensityInGbps";
	private static final String ATTNAMESUFFIX_USERSERVICENAME = "userServiceName";
	private static final String ATTNAMESUFFIX_ISUPSTREAM = "isUpstream";
	private static final String ATTNAMESUFFIX_VALIDINPUTNODENAMES = "validInputNodeNames";
	private static final String ATTNAMESUFFIX_VALIDOUTPUTNODENAMES = "validOutputNodeNames";
	private static final String ATTNAMESUFFIX_SEQUENCEOFEXPANSIONFACTORRESPECTTOINJECTION = "sequenceOfPerVnfExpansionFactorsRespectToInjection";
	private static final String ATTNAMESUFFIX_LISTMAXLATENCYFROMINITIALTOVNFSTART_MS = "limitMaxLatencyFromInitialToVnfStart_ms";

	private final Demand sc; // from anycastIN to anycastOUT
	
	WServiceChainRequest(Demand sc) 
	{ 
		super(sc); 
		this.sc = sc; 
		assert sc.getLayer().getIndex() == 1;
		assert new WNode (sc.getIngressNode()).isVirtualNode();
		assert new WNode (sc.getEgressNode()).isVirtualNode();
	}

	public int getNumberVnfsToTraverse () { return sc.getServiceChainSequenceOfTraversedResourceTypes().size(); }
	
	public List<Double> getListMaxLatencyFromInitialToVnStart_ms ()
	{
		final int numVnfs = getNumberVnfsToTraverse();
		if (numVnfs == 0) return Arrays.asList();
		final Double [] defaultVal = new Double [getNumberVnfsToTraverse()]; Arrays.fill(defaultVal, Double.MAX_VALUE);
		final List<Double> res = getAttributeAsListDoubleOrDefault(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_LISTMAXLATENCYFROMINITIALTOVNFSTART_MS, Arrays.asList(defaultVal));
		return res.stream().map(v->v <= 0? Double.MAX_VALUE : v).collect(Collectors.toCollection(ArrayList::new));
	}
	public void setListMaxLatencyFromInitialToVnfStart_ms (List<Double>  maxLatencyList_ms) 
	{
		final int numVnfs = getNumberVnfsToTraverse();
		if (maxLatencyList_ms.size() != numVnfs) throw new Net2PlanException ("Wrong number of latency factors");
		getNe().setAttributeAsNumberList(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_LISTMAXLATENCYFROMINITIALTOVNFSTART_MS, (List<Number>) (List<?>) maxLatencyList_ms);
	}
	
	public List<Double> getSequenceOfExpansionFactorsRespectToInjection () 
	{
		final int numVnfs = getNumberVnfsToTraverse();
		if (numVnfs == 0) return Arrays.asList();
		final Double [] defaultVal = new Double [getNumberVnfsToTraverse()]; Arrays.fill(defaultVal, 1.0);
		return getAttributeAsListDoubleOrDefault(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_SEQUENCEOFEXPANSIONFACTORRESPECTTOINJECTION, Arrays.asList(defaultVal));
	}

	public WServiceChain addServiceChain (List<? extends WAbstractNetworkElement> sequenceOfIpLinksAndResources , double injectionTrafficGbps)
	{
		final Pair<List<NetworkElement> , List<Double>> npInfo = WServiceChain.computeScPathAndOccupationInformationAndCheckValidity(this, injectionTrafficGbps, sequenceOfIpLinksAndResources);
		final Route r = getNe().getNetPlan().addServiceChain(this.sc, injectionTrafficGbps, npInfo.getSecond(), npInfo.getFirst(), null);
		return new WServiceChain(r);
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

	public void setPotentiallyValidOrigins (SortedSet<WNode> validOrigins) 
	{ 
		final List<String> resNames = validOrigins.stream().map(n->n.getName()).collect(Collectors.toList()); 
		sc.setAttributeAsStringList(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_VALIDINPUTNODENAMES , resNames);
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
	public void setPotentiallyValidDestinations (SortedSet<WNode> validDestinations) 
	{ 
		final List<String> resNames = validDestinations.stream().map(n->n.getName()).collect(Collectors.toList()); 
		sc.setAttributeAsStringList(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_VALIDOUTPUTNODENAMES , resNames);
	}

	public Optional<Double> getTrafficIntensityInfoOrDefault (String timeSlotName)
	{
		return getFullTrafficIntensityInfo().stream().filter(p->p.getFirst().equals(timeSlotName)).map(p->p.getSecond()).findFirst();  
	}
	
	public Pair<String,Double> getTrafficIntensityInfoOrDefault (int timeSlotIndex)
	{
		final List<Pair<String,Double>> res = getFullTrafficIntensityInfo();
		if (res.size() <= timeSlotIndex) throw new Net2PlanException ("Wrong index");  
		return res.get(timeSlotIndex);  
	}

	public List<String> getSequenceVnfTypes () { return sc.getServiceChainSequenceOfTraversedResourceTypes(); }
	
	public String getUserServiceName () 
	{ 
		final String res = getAttributeOrDefault(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_USERSERVICENAME , null);
		assert res != null; 
		return res;
	}
	public void setUserServiceName (String userServiceName) 
	{ 
		getNe().setAttribute(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_USERSERVICENAME , userServiceName);
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
	
	public SortedMap<String,Double> getTimeSlotAndInitialInjectionIntensityInGbpsMap ()
	{
		final SortedMap<String , Double> res = new TreeMap<> ();
		final List<List<String>> matrix = getNe().getAttributeAsStringMatrix(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_TIMESLOTANDINTENSITYINGBPS, null);
		if (matrix == null) throw new Net2PlanException("Wrong format");
		for (List<String> row : matrix)
		{
			if (row.size() != 2) throw new Net2PlanException ("Wrong format");
			final String timeZoneName = row.get(0);
			final double val = Double.parseDouble(row.get(1));
			res.put(timeZoneName, val);
		}
		return res;
	}

	public void setTimeSlotAndInitialInjectionIntensityInGbpsMap (SortedMap<String,Double> timeSlotAndInitialInjectionIntensityInGbpsMap)
	{
		final List<List<String>> matrix = new ArrayList<> ();
		for (Entry<String , Double> entry : timeSlotAndInitialInjectionIntensityInGbpsMap.entrySet())
		{
			final List<String> infoThisVnf = new LinkedList<> ();
			infoThisVnf.add(entry.getKey());
			infoThisVnf.add(entry.getValue() + "");
			matrix.add(infoThisVnf);
		}
		getNet().getNe().setAttributeAsStringMatrix(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_TIMESLOTANDINTENSITYINGBPS, matrix);
	}

	public void setCurrentOfferedTrafficInGbps (String timeSlotName) 
	{
		final Double trafficInjectedInGbps = getTimeSlotAndInitialInjectionIntensityInGbpsMap ().getOrDefault(timeSlotName, null);
		if (trafficInjectedInGbps == null) throw new Net2PlanException ("Unexisting time slot name");
		sc.setOfferedTraffic(trafficInjectedInGbps); 
	}

	public boolean isUpstream () { final Boolean res = getAttributeAsBooleanOrDefault(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_ISUPSTREAM, null); assert res != null; return res; }
	public void setIsUpstream (boolean isUpstream) 
	{ 
		setAttributeAsBoolean(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_ISUPSTREAM, isUpstream); 
	}
	
	@Override
	public Demand getNe() { return (Demand) e; }

}
