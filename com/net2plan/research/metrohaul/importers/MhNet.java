package com.net2plan.research.metrohaul.importers;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetworkElement;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.interfaces.networkDesign.Resource;

public class MhNet
{
	public class WNode 
	{
		private static final String ATTNAMECOMMONPREFIX = "Node_";
		private static final String ATTNAMESUFFIX_TYPE = "type";
		private static final String ATTNAMESUFFIX_ISCONNECTEDTOCORE = "isConnectedToNetworkCore";
		private static final String RESOURCETYPE_CPU = "CPU";
		private static final String RESOURCETYPE_RAM = "RAM";
		private static final String RESOURCETYPE_HD = "HD";
		
		private final Node n;
		public WNode (Node n) { this.n = n; }
		public String getName () { return n.getName(); }
		public void setName (String name) { n.setName(name); }
		public Point2D getNodePositionXY () { return n.getXYPositionMap(); }
		public void setNodePositionXY (Point2D position) { n.setXYPositionMap(position); }
		public String getType () { return getAttributeOrFail(n, ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_TYPE); }
		public void setType (String type) { n.setAttribute(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_TYPE , type); }
		public boolean isConnectedToNetworkCore () { return getAttributeAsBooleanOrFail(n, ATTNAMECOMMONPREFIX +ATTNAMESUFFIX_ISCONNECTEDTOCORE); }
		public void setIsConnectedToNetworkCore (boolean isConnectedToCore) { n.setAttribute(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_ISCONNECTEDTOCORE , new Boolean (isConnectedToCore).toString()); }
		public double getPopulation () { return n.getPopulation(); }
		public void setPoputlation (double population) { n.setPopulation(population); }
		public double getTotalNumCpus () { return n.getResources(RESOURCETYPE_CPU).stream().mapToDouble(r->r.getCapacity()).sum (); }
		public void setTotalNumCpus (double totalNumCpus) 
		{ 
			final Set<Resource> res = n.getResources(RESOURCETYPE_CPU);
			if (res.size() > 1) throw new Net2PlanException ("Format error");
			if (res.isEmpty()) 
				res.add(n.getNetPlan().addResource(RESOURCETYPE_CPU, RESOURCETYPE_CPU, n, totalNumCpus, RESOURCETYPE_CPU, new HashMap<> (), 0.0, null));
			else 
				res.iterator().next().setCapacity(totalNumCpus, new HashMap<> ());
		}
		public double getTotalRamGB () { return n.getResources(RESOURCETYPE_RAM).stream().mapToDouble(r->r.getCapacity()).sum (); }
		public void setTotalRamGB (double totalRamGB) 
		{ 
			final Set<Resource> res = n.getResources(RESOURCETYPE_RAM);
			if (res.size() > 1) throw new Net2PlanException ("Format error");
			if (res.isEmpty()) 
				res.add(n.getNetPlan().addResource(RESOURCETYPE_RAM, RESOURCETYPE_RAM, n, totalRamGB, "GB", new HashMap<> (), 0.0, null));
			else 
				res.iterator().next().setCapacity(totalRamGB, new HashMap<> ());
		}
		public double getTotalHdGB () { return n.getResources(RESOURCETYPE_HD).stream().mapToDouble(r->r.getCapacity()).sum (); }
		public void setTotalHdGB (double totalHdGB) 
		{ 
			final Set<Resource> res = n.getResources(RESOURCETYPE_HD);
			if (res.size() > 1) throw new Net2PlanException ("Format error");
			if (res.isEmpty()) 
				res.add(n.getNetPlan().addResource(RESOURCETYPE_HD, RESOURCETYPE_HD, n, totalHdGB, "GB", new HashMap<> (), 0.0, null));
			else 
				res.iterator().next().setCapacity(totalHdGB, new HashMap<> ());
		}
	}
	
	public class WFiber 
	{
		private static final String ATTNAMECOMMONPREFIX = "Fiber_";
		private static final String ATTNAMESUFFIX_ATTENUATIONCOEFFICIENTDBPERKM = "FiberAttenuationCoefficient_dbPerKm";
		private static final String ATTNAMESUFFIX_AMPLIFIERPOSITIONFROMORIGINNODE_KM = "AmplifierPositionsFromOriginNode_km";
		private static final String ATTNAMESUFFIX_AMPLIFIERGAINS_DB = "AmplifierGains_dB";
		
		private final Link e;
		public WFiber (Link e) { this.e = e; }
		public WNode getA () { return new WNode (e.getOriginNode()); }
		public WNode getB () { return new WNode (e.getDestinationNode()); }
		public double getLengthInKm () { return e.getLengthInKm(); }
		public void setLenghtInKm (double lenghtInKm) { e.setLengthInKm(lenghtInKm); }
		public boolean isBidirectional () { return e.isBidirectional(); }
		public double getAttenuationCoefficient_dbPerKm () { return getAttributeAsDoubleOrFail(e, ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_ATTENUATIONCOEFFICIENTDBPERKM); } 
		public void setAttenuationCoefficient_dbPerKm (double attenuationCoef_dbPerKm) { e.setAttribute(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_ATTENUATIONCOEFFICIENTDBPERKM , new Double (attenuationCoef_dbPerKm).toString()); }
		public WFiber getBidirectionalPair () { if (!this.isBidirectional()) throw new Net2PlanException ("Not a bidirectional link"); return new WFiber (e.getBidirectionalPair()); }
		public List<Double> getAmplifierPositionsKmFromOrigin_km () { return getAttributeAsListDoubleOrFail (e , ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_AMPLIFIERPOSITIONFROMORIGINNODE_KM); }
		public void setAmplifierPositionsKmFromOrigin_km (List<Double> positions) 
		{
			if (positions.stream().anyMatch(p->p<0 || p>getLengthInKm())) throw new Net2PlanException ("Amplifier outside of position");
			e.setAttributeAsNumberList(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_AMPLIFIERPOSITIONFROMORIGINNODE_KM , (List<Number>) (List<?>) positions); 
		}
		public List<Double> getAmplifierGains_dB () { return getAttributeAsListDoubleOrFail (e , ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_AMPLIFIERGAINS_DB); }
		public void setAmplifierGains_dB (List<Double> gains_db) 
		{
			e.setAttributeAsNumberList(ATTNAMECOMMONPREFIX + ATTNAMESUFFIX_AMPLIFIERGAINS_DB , (List<Number>) (List<?>) gains_db); 
		}
	}

	
	
	private static String getAttributeOrFail (NetworkElement e , String name) { final String s = e.getAttribute(name); if (s == null) throw new Net2PlanException ("Unexisting attribute: " + name); return s; }    
	private static boolean getAttributeAsBooleanOrFail (NetworkElement e , String name) { final String s = e.getAttribute(name); if (s == null) throw new Net2PlanException ("Error reading attribute: " + name); try { return Boolean.parseBoolean (s); } catch (Exception ee) { throw new Net2PlanException ("Error reading attribute: " + name); }  }    
	private static double getAttributeAsDoubleOrFail (NetworkElement e , String name) { final String s = e.getAttribute(name); if (s == null) throw new Net2PlanException ("Error reading attribute: " + name); try { return Double.parseDouble(s); } catch (Exception ee) { throw new Net2PlanException ("Error reading attribute: " + name); }  }    
	private static int getAttributeAsIntegerOrFail (NetworkElement e , String name) { final String s = e.getAttribute(name); if (s == null) throw new Net2PlanException ("Error reading attribute: " + name); try { return Integer.parseInt(s); } catch (Exception ee) { throw new Net2PlanException ("Error reading attribute: " + name); }  }    
	private static List<Double> getAttributeAsListDoubleOrFail (NetworkElement e , String name) { final List<Double> res = e.getAttributeAsDoubleList(name , null); if (res == null) throw new Net2PlanException ("Error reading attribute: " + name); return res;  }    
	
}
