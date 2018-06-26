package com.net2plan.research.metrohaul.networkModel;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import com.net2plan.interfaces.networkDesign.Resource;

/** An instance of this class is a VNF instance hosted in a node, of a given type, with a given capacity. The characteristics of the 
 * VNF instance are:
 * <ul>
 * <li>Hosting node: the node where this VNF instance exists</li>
 * <li>Type: The type of VNF</li>
 * <li>Name: A user-defined string with the name of the VNF instance</li>
 * <li>Processing time in ms: The processing time added to the traversing traffic.</li>
 * <li>CPU, RAM, HD consumed: The amount of such resources of the node consumed by the VNF instance</li>
 * <li>Capacity: measured as the amount of input Gbps that this VNF instance can process</li>
 * <li></li>
 * <li></li>
 * <li></li>
 * </ul>
 */
public class WVnfInstance extends WAbstractNetworkElement
{
	WVnfInstance(Resource r) { super (r); this.r = r; }

	final private Resource r;

	@Override
	public Resource getNe() { return r; }
	
	/** Returns the hosting node of the VNF instance
	 * @return
	 */
	public WNode getHostingNode () { return new WNode (r.getHostNode()); }
	
	/** Returns the VNF type of this instance
	 * @return
	 */
	public String getType () { return r.getType(); }
	
	/** Returns the user-defined name of this instance
	 * @return
	 */
	public String getName () { return r.getName(); }
	/** Sets the user-defined name for this instance
	 * @param name
	 */
	public void setName (String name) { r.setName(name); }
	
	/** Returns the processing time in ms added to the traversing traffic to account for the end-to-end latencies
	 * @return
	 */
	public double getProcessingTimeInMs () { return r.getProcessingTimeToTraversingTrafficInMs(); }
	/** Sets the processing time in ms added to the traversing traffic to account for the end-to-end latencies
	 * @return
	 */
	public void setProcessingTimeInMs (double procTimeInMs) { r.setProcessingTimeToTraversingTrafficInMs(procTimeInMs); }
	
	/** Returns the service chains that are traversing this VNF instance
	 * @return
	 */
	public SortedSet<WServiceChain> getTraversingServiceChains () { return r.getTraversingRoutes().stream().map(rr->new WServiceChain(rr)).collect(Collectors.toCollection(TreeSet::new)); }

	/** Returns the service chains requests that have at least one service chain that is traversing this VNF instance
	 * @return
	 */
	public SortedSet<WServiceChainRequest> getTraversingServiceChainRequests () { return r.getTraversingRoutes().stream().map(rr->new WServiceChainRequest(rr.getDemand())).collect(Collectors.toCollection(TreeSet::new)); }

	/** Returns the VNF capacity (measured as Gbps of the traversing service chains, at the input of the VNF) that is 
	 * currently consumed by the traversing service chains 
	 * @return
	 */
	public double getOccupiedCapacityInGbps () { return r.getOccupiedCapacity(); }
	
	/** Returns the VNF capacity occupied by a given service chain, or zero if (i) the service chain is down, (ii) the service chain is not traversing this VNF instance.
	 * The VNF capacity occupied is the service chain traffic at the input of the VNF   
	 * @param sc
	 * @return
	 */
	public double getOccupiedCapacityByTraversingRouteInGbps (WServiceChain sc) 
	{
		return r.getTraversingRouteOccupiedCapacity(sc.getNe());
	}

	/** Sets the VNF capacity of this instance
	 * @param newProcessingCapacityInGbps the new processing capacity of the VNF, measured as the maximum amount of Gbps considering the traffic of the traversing service chains, at the input of the VNF 
	 * @param newOccupiedCpu the amount of CPU occupied by this instance
	 * @param newOccupiedRam the amount of RAM in GBytes occupied by this instance
	 * @param newOccupiedHd the amount of hard disk in Gbytes occupied by this instance
	 */
	public void setCapacityInGbpsOfInputTraffic (double newProcessingCapacityInGbps , double newOccupiedCpu , double newOccupiedRam , double newOccupiedHd)
	{
		final Map<Resource,Double> map = new HashMap<> ();
		map.put (getHostingNode().getCpuBaseResource() , newOccupiedCpu);
		map.put (getHostingNode().getRamBaseResource() , newOccupiedRam);
		map.put (getHostingNode().getHdBaseResource() , newOccupiedHd);		
		r.setCapacity(newProcessingCapacityInGbps, map);
	}
	
	/** Returns the current capacity of this VNF
	 * @return
	 */
	public double getCurrentCapacityInGbps () { return r.getCapacity(); } 
	
	/** Returns the amount of occupied CPUs by this VNf instance
	 * @return
	 */
	public double getOccupiedCpus () 
	{ 
		return r.getCapacityOccupiedInBaseResource(getHostingNode().getCpuBaseResource());
	}
	/** Returns the amount of occupied hard-disk in Gbytes by this VNf instance
	 * @return
	 */
	public double getOccupiedHdInGB () 
	{ 
		return r.getCapacityOccupiedInBaseResource(getHostingNode().getHdBaseResource());
	}
	/** Returns the amount of occupied RAM in Gbytes by this VNf instance
	 * @return
	 */
	public double getOccupiedRamInGB () 
	{ 
		return r.getCapacityOccupiedInBaseResource(getHostingNode().getRamBaseResource());
	}
	
}
