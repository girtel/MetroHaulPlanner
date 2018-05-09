package com.net2plan.research.metrohaul.networkModel;

import com.net2plan.interfaces.networkDesign.Route;

public class WLightpath extends WAbstractNetworkElement
{
	public Route getNe () { return (Route) e; }
	public WLightpath (Route r) { super (r);  }
	public WNode getA () { return new WNode (getNe ().getIngressNode()); }
	public WNode getB () { return new WNode (getNe ().getEgressNode()); }
	public WLightpathRequest getLightpathRequest () { return new WLightpathRequest(getNe ().getDemand()); }
	public boolean isBackupLightpath () { return getNe ().isBackupRoute(); }
	public boolean isMainLightpathWithBackupRoutes () { return getNe ().hasBackupRoutes(); }
	public void addBackupLightpath (WLightpath backup) { getNe().addBackupRoute(backup.getNe ()); }
	public void removeBackupLightpath (WLightpath backup) { getNe().removeBackupRoute(backup.getNe ()); }

}
