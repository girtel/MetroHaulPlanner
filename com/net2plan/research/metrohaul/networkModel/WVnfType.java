package com.net2plan.research.metrohaul.networkModel;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import com.net2plan.utils.Quadruple;
import com.net2plan.utils.Triple;

public class WVnfType
{
	final private String vnfTypeName;
	final private double maxInputTrafficPerVnfInstance_Gbps;
	final private double occupCpu, occupRam, occupHd;
	final private boolean isConstrained;
	final private SortedSet<String> validMetroNodesForInstantiation;
	private String arbitraryParamString;
	public WVnfType (String vnfType , Quadruple<Double , Triple<Double,Double,Double> , Boolean , SortedSet<String>> info , String arbitraryParamString)
	{
		this.vnfTypeName = vnfType;
		this.maxInputTrafficPerVnfInstance_Gbps = info.getFirst();
		this.occupCpu = info.getSecond().getFirst();
		this.occupRam = info.getSecond().getSecond();
		this.occupHd = info.getSecond().getThird();
		this.isConstrained = info.getThird();
		this.validMetroNodesForInstantiation = new TreeSet<> (info.getFourth());
		this.arbitraryParamString = arbitraryParamString;
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
	public SortedSet<String> getValidMetroNodesForInstantiation()
	{
		return Collections.unmodifiableSortedSet(validMetroNodesForInstantiation);
	}
	public String getArbitraryParamString()
	{
		return arbitraryParamString;
	}
	public void setArbitraryParamString (String arbitraryParamString) { this.arbitraryParamString = arbitraryParamString; }
	
}
