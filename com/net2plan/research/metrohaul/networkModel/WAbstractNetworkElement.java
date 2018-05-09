package com.net2plan.research.metrohaul.networkModel;

import java.util.List;

import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.NetworkElement;

public abstract class WAbstractNetworkElement implements Comparable<WAbstractNetworkElement>  
{
	protected final NetworkElement e;
	
	protected WAbstractNetworkElement (NetworkElement e) 
	{
		if (e == null) throw new Net2PlanException ("Null element");
		this.e = e; 
	}
	public abstract NetworkElement getNe ();
	
	protected String getAttributeOrFail (String name) { final String s = e.getAttribute(name); if (s == null) throw new Net2PlanException ("Unexisting attribute: " + name); return s; }    
	protected boolean getAttributeAsBooleanOrFail (String name) { final String s = e.getAttribute(name); if (s == null) throw new Net2PlanException ("Error reading attribute: " + name); try { return Boolean.parseBoolean (s); } catch (Exception ee) { throw new Net2PlanException ("Error reading attribute: " + name); }  }    
	protected double getAttributeAsDoubleOrFail (String name) { final String s = e.getAttribute(name); if (s == null) throw new Net2PlanException ("Error reading attribute: " + name); try { return Double.parseDouble(s); } catch (Exception ee) { throw new Net2PlanException ("Error reading attribute: " + name); }  }    
	protected int getAttributeAsIntegerOrFail (String name) { final String s = e.getAttribute(name); if (s == null) throw new Net2PlanException ("Error reading attribute: " + name); try { return Integer.parseInt(s); } catch (Exception ee) { throw new Net2PlanException ("Error reading attribute: " + name); }  }    
	protected List<Double> getAttributeAsListDoubleOrFail (String name) { final List<Double> res = e.getAttributeAsDoubleList(name , null); if (res == null) throw new Net2PlanException ("Error reading attribute: " + name); return res;  }    

	public NetPlan getNetPlan () { return e.getNetPlan(); }
	public boolean wasRemoved () { return e.wasRemoved(); }
	public long getId () { return e.getId(); }
	
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
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
