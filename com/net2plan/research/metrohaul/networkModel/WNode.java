package com.net2plan.research.metrohaul.networkModel;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Set;

import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.interfaces.networkDesign.Resource;

public class WNode extends WAbstractNetworkElement
{
	private static final String ATTNAMECOMMONPREFIX = "Node_";
	private static final String ATTNAMESUFFIX_TYPE = "type";
	private static final String ATTNAMESUFFIX_ISCONNECTEDTOCORE = "isConnectedToNetworkCore";
	private static final String RESOURCETYPE_CPU = "CPU";
	private static final String RESOURCETYPE_RAM = "RAM";
	private static final String RESOURCETYPE_HD = "HD";
	
	public Node getNe () { return (Node) e; }
	public WNode (Node n) { super (n); }
	public String getName () { return getNe().getName(); }
	public void setName (String name) { getNe().setName(name); }
	public Point2D getNodePositionXY () { return getNe().getXYPositionMap(); }
	public void setNodePositionXY (Point2D position) { getNe().setXYPositionMap(position); }
	public String getType () { return getAttributeOrFail(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_TYPE); }
	public void setType (String type) { getNe().setAttribute(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_TYPE , type); }
	public boolean isConnectedToNetworkCore () { return getAttributeAsBooleanOrFail(ATTNAMECOMMONPREFIX +ATTNAMESUFFIX_ISCONNECTEDTOCORE); }
	public void setIsConnectedToNetworkCore (boolean isConnectedToCore) { getNe().setAttribute(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_ISCONNECTEDTOCORE , new Boolean (isConnectedToCore).toString()); }
	public double getPopulation () { return getNe().getPopulation(); }
	public void setPoputlation (double population) { getNe().setPopulation(population); }
	public double getTotalNumCpus () { return getNe().getResources(RESOURCETYPE_CPU).stream().mapToDouble(r->r.getCapacity()).sum (); }
	public void setTotalNumCpus (double totalNumCpus) 
	{ 
		final Set<Resource> res = getNe().getResources(RESOURCETYPE_CPU);
		if (res.size() > 1) throw new Net2PlanException ("Format error");
		if (res.isEmpty()) 
			res.add(getNe().getNetPlan().addResource(RESOURCETYPE_CPU, RESOURCETYPE_CPU, getNe(), totalNumCpus, RESOURCETYPE_CPU, new HashMap<> (), 0.0, null));
		else 
			res.iterator().next().setCapacity(totalNumCpus, new HashMap<> ());
	}
	public double getTotalRamGB () { return getNe().getResources(RESOURCETYPE_RAM).stream().mapToDouble(r->r.getCapacity()).sum (); }
	public void setTotalRamGB (double totalRamGB) 
	{ 
		final Set<Resource> res = getNe().getResources(RESOURCETYPE_RAM);
		if (res.size() > 1) throw new Net2PlanException ("Format error");
		if (res.isEmpty()) 
			res.add(getNe().getNetPlan().addResource(RESOURCETYPE_RAM, RESOURCETYPE_RAM, getNe(), totalRamGB, "GB", new HashMap<> (), 0.0, null));
		else 
			res.iterator().next().setCapacity(totalRamGB, new HashMap<> ());
	}
	public double getTotalHdGB () { return getNe().getResources(RESOURCETYPE_HD).stream().mapToDouble(r->r.getCapacity()).sum (); }
	public void setTotalHdGB (double totalHdGB) 
	{ 
		final Set<Resource> res = getNe().getResources(RESOURCETYPE_HD);
		if (res.size() > 1) throw new Net2PlanException ("Format error");
		if (res.isEmpty()) 
			res.add(getNe().getNetPlan().addResource(RESOURCETYPE_HD, RESOURCETYPE_HD, getNe(), totalHdGB, "GB", new HashMap<> (), 0.0, null));
		else 
			res.iterator().next().setCapacity(totalHdGB, new HashMap<> ());
	}
}
