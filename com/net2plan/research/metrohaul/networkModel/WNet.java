package com.net2plan.research.metrohaul.networkModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.net2plan.interfaces.networkDesign.NetPlan;

public class WNet extends WAbstractNetworkElement
{
	public WNet (NetPlan np) { super (np); this.np = np; }
	public final NetPlan np;

	@Override
	public NetPlan getNe() { return (NetPlan) e;  }

	public WLayerWdm getWdmLayer () { return new WLayerWdm(np.getNetworkLayer(0)); }
	public List<WNode> getNodes () { return np.getNodes().stream().map(n->new WNode(n)).collect(Collectors.toCollection(ArrayList::new));  }
	public List<WFiber> getFibers () { return np.getLinks(getWdmLayer().getNe()).stream().map(n->new WFiber(n)).collect(Collectors.toCollection(ArrayList::new));  }
	public List<WLightpathRequest> getLightpathRequests () { return np.getDemands(getWdmLayer().getNe()).stream().map(n->new WLightpathRequest(n)).collect(Collectors.toCollection(ArrayList::new));  }
	public List<WLightpath> getLightpaths () { return np.getRoutes(getWdmLayer().getNe()).stream().map(n->new WLightpath(n)).collect(Collectors.toCollection(ArrayList::new));  }
	
}
