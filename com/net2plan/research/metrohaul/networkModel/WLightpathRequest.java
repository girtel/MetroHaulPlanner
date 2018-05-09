package com.net2plan.research.metrohaul.networkModel;

import java.util.List;
import java.util.stream.Collectors;

import com.net2plan.interfaces.networkDesign.Demand;

public class WLightpathRequest extends WAbstractNetworkElement
{
	public Demand getNe () { return (Demand) e; }
	public WLightpathRequest (Demand d) { super (d);  }
	public WNode getA () { return new WNode (getNe().getIngressNode()); }
	public WNode getB () { return new WNode (getNe().getEgressNode()); }
	public boolean isBidirectional () { return getNe().isBidirectional(); }
	public List<WLightpath> getLightpaths () { return getNe().getRoutes().stream().map(r->new WLightpath(r)).collect(Collectors.toList()); }
	
	
}
