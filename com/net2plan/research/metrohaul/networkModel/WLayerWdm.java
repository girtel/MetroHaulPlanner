package com.net2plan.research.metrohaul.networkModel;

import com.net2plan.interfaces.networkDesign.NetworkLayer;

public class WLayerWdm extends WAbstractNetworkElement
{
	final private NetworkLayer wdmLayer;

	WLayerWdm (NetworkLayer l) { super (l); this.wdmLayer = l; }

	@Override
	public NetworkLayer getNe() { return wdmLayer; }
	
    
	
}
