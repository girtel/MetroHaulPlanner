package com.net2plan.research.metrohaul.networkModel;

import java.util.SortedSet;

public class WVnfType
{
	final private String vnfTypeName;
	final private double maxInputTrafficPerVnfInstance_Gbps;
	final private double occupCpu, occupRam, occupHd;
	final private boolean isConstrained;
	final private SortedSet<String> validMetroNodesForInstantiation;
	private String arbitraryParamString;
	public WVnfType(String vnfTypeName, double maxInputTrafficPerVnfInstance_Gbps, double occupCpu, double occupRam,
			double occupHd, boolean isConstrained, SortedSet<String> validMetroNodesForInstantiation,
			String arbitraryParamString)
	{
		super();
		this.vnfTypeName = vnfTypeName;
		this.maxInputTrafficPerVnfInstance_Gbps = maxInputTrafficPerVnfInstance_Gbps;
		this.occupCpu = occupCpu;
		this.occupRam = occupRam;
		this.occupHd = occupHd;
		this.isConstrained = isConstrained;
		this.validMetroNodesForInstantiation = validMetroNodesForInstantiation;
		this.arbitraryParamString = arbitraryParamString;
	}
	public String getArbitraryParamString()
	{
		return arbitraryParamString;
	}
	public void setArbitraryParamString(String arbitraryParamString)
	{
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
		return validMetroNodesForInstantiation;
	}
	
	
	
//	public WVnfType (String vnfType , Quadruple<Double , Triple<Double,Double,Double> , Boolean , SortedSet<String>> info , String arbitraryParamString)
//	{
//		this.vnfTypeName = vnfType;
//		this.maxInputTrafficPerVnfInstance_Gbps = info.getFirst();
//		this.occupCpu = info.getSecond().getFirst();
//		this.occupRam = info.getSecond().getSecond();
//		this.occupHd = info.getSecond().getThird();
//		this.isConstrained = info.getThird();
//		this.validMetroNodesForInstantiation = new TreeSet<> (info.getFourth());
//		this.arbitraryParamString = arbitraryParamString;
//	}
//	public String getVnfTypeName()
//	{
//		return vnfTypeName;
//	}
//	public double getMaxInputTrafficPerVnfInstance_Gbps()
//	{
//		return maxInputTrafficPerVnfInstance_Gbps;
//	}
//	public double getOccupCpu()
//	{
//		return occupCpu;
//	}
//	public double getOccupRam()
//	{
//		return occupRam;
//	}
//	public double getOccupHd()
//	{
//		return occupHd;
//	}
//	public boolean isConstrained()
//	{
//		return isConstrained;
//	}
//	public SortedSet<String> getValidMetroNodesForInstantiation()
//	{
//		return Collections.unmodifiableSortedSet(validMetroNodesForInstantiation);
//	}
//	public String getArbitraryParamString()
//	{
//		return arbitraryParamString;
//	}
//	public void setArbitraryParamString (String arbitraryParamString) { this.arbitraryParamString = arbitraryParamString; }
	
}
