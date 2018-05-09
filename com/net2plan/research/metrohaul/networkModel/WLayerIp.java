package com.net2plan.research.metrohaul.networkModel;

import com.net2plan.interfaces.networkDesign.NetworkLayer;

public class WLayerIp extends WAbstractNetworkElement
{
	final private NetworkLayer ipLayer;

	public WLayerIp  (NetworkLayer l) { super (l); this.ipLayer = l; }

	@Override
	public NetworkLayer getNe() { return ipLayer; }

}
