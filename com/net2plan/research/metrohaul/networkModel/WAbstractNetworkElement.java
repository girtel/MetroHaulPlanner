package com.net2plan.research.metrohaul.networkModel;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.NetworkElement;
import com.net2plan.interfaces.networkDesign.NetworkLayer;
import com.net2plan.interfaces.networkDesign.Resource;

public abstract class WAbstractNetworkElement implements Comparable<WAbstractNetworkElement>  
{
	protected final NetworkElement e;
	
	protected WAbstractNetworkElement (NetworkElement e) 
	{
		if (e == null) throw new Net2PlanException ("Null element");
		this.e = e; 
	}
	public abstract NetworkElement getNe ();
	
	protected String getAttributeOrDefault (String name , String defaultValue) { return e.getAttribute(name, defaultValue);  }    
	protected boolean getAttributeAsBooleanOrDefault (String name , Boolean defaultValue) { final String s = e.getAttribute(name); if (s == null) return defaultValue; try { return Boolean.parseBoolean (s); } catch (Exception ee) { return defaultValue; }  }    
	protected void setAttributeAsBoolean (String name , Boolean value) { e.setAttribute(name, value.toString()); }    
	protected double getAttributeAsDoubleOrDefault (String name , Double defaultValue) { return e.getAttributeAsDouble(name, defaultValue); }    
	protected int getAttributeAsIntegerOrDefault (String name , Integer defaultValue) { final String s = e.getAttribute(name); if (s == null) return defaultValue; try { return Integer.parseInt(s); } catch (Exception ee) { return defaultValue; }  }    
	protected List<Double> getAttributeAsListDoubleOrDefault (String name , List<Double> defaultValue) { return e.getAttributeAsDoubleList(name, defaultValue);}    
	protected SortedSet<Integer> getAttributeAsSortedSetIntegerOrDefault (String name , SortedSet<Integer> defaultValue) { final List<Double> res = e.getAttributeAsDoubleList(name , null); if (res == null) return defaultValue; return res.stream().map(nn->new Integer(nn.intValue())).collect(Collectors.toCollection(TreeSet::new));  }    

	NetworkLayer getIpNpLayer () { return e.getNetPlan().getNetworkLayer(1); }
	NetworkLayer getWdmNpLayer () { return e.getNetPlan().getNetworkLayer(0); }
	public NetPlan getNetPlan () { return e.getNetPlan(); }
	public boolean wasRemoved () { return e.wasRemoved(); }
	public long getId () { return e.getId(); }
	public WNet getNet () { return new WNet (getNetPlan()); }
	public boolean isVnfInstance () 
	{ 
		return (e instanceof Resource);  
	}
	public boolean isWIpLink () { if (!(e instanceof Link)) return false; return ((Link) e).getLayer().getIndex() == 1; }
	
    @Override
    public final boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (this == obj) return true;
        if (getClass() != obj.getClass()) return false;
        WAbstractNetworkElement other = (WAbstractNetworkElement) obj;
        if (e == null)
        {
            if (other.e != null) return false;
        } else if (!e.equals(other.e)) return false;
        return true;
    }

    @Override
    public final int compareTo(WAbstractNetworkElement o)
	{
	    if (this.equals(o)) return 0;
	    if (o == null) throw new NullPointerException ();
	    if (this.getNetPlan() != o.getNetPlan()) throw new Net2PlanException ("Different Mtn!");
	    return Long.compare(this.getId(), o.getId());
	}
}
