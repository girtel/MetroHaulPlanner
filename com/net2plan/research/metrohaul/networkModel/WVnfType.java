package com.net2plan.research.metrohaul.networkModel;

import java.util.SortedSet;
import java.util.TreeSet;

import com.net2plan.utils.Quadruple;
import com.net2plan.utils.Triple;

public class WVnfType
{
	final private String vnfTypeName;
	final double maxInputTrafficPerVnfInstance_Gbps;
	final double occupCpu, occupRam, occupHd;
	final boolean isConstrained;
	final SortedSet<WNode> validMetroNodesForInstantiation;
	public WVnfType (String vnfType , Quadruple<Double , Triple<Double,Double,Double> , Boolean , SortedSet<WNode>> info)
	{
		this.vnfTypeName = vnfType;
		this.maxInputTrafficPerVnfInstance_Gbps = info.getFirst();
		this.occupCpu = info.getSecond().getFirst();
		this.occupRam = info.getSecond().getSecond();
		this.occupHd = info.getSecond().getThird();
		this.isConstrained = info.getThird();
		this.validMetroNodesForInstantiation = new TreeSet<> (info.getFourth());
		
	}
	public String getVnfTypeName()
	{
		return vnfTypeName;
	}
	public double getMaxInputTrafficPerVnfInstance_Gbps()
	{
		return maxInputTrafficPerVnfInstance_Gbps;
	}
	public double getOccupCpu()
	{
		return occupCpu;
	}
	public double getOccupRam()
	{
		return occupRam;
	}
	public double getOccupHd()
	{
		return occupHd;
	}
	public boolean isConstrained()
	{
		return isConstrained;
	}
	public SortedSet<WNode> getValidMetroNodesForInstantiation()
	{
		return validMetroNodesForInstantiation;
	}
	
}
