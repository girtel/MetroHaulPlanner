package com.net2plan.research.metrohaul.networkModel;

import java.util.SortedSet;

/** This class represents the information of a type of VNF defined for the network. This is used when a VNF is instantiated in a node, since 
 * instantiated VNFs must be of a previously defined type. 
 */
/**
 * @author Pablo
 *
 */
public class WVnfType
{
	final private String vnfTypeName;
	final private double maxInputTrafficPerVnfInstance_Gbps;
	final private double occupCpu, occupRamGB, occupHdGB;
	final private boolean isConstrainedToBeInstantiatedOnlyInUserDefinedNodes;
	final private SortedSet<String> validMetroNodesForInstantiation;
	private String arbitraryParamString;

	WVnfType(String vnfTypeName, double maxInputTrafficPerVnfInstance_Gbps, double occupCpu, double occupRam,
			double occupHd, boolean isConstrained, SortedSet<String> validMetroNodesForInstantiation,
			String arbitraryParamString)
	{
		super();
		this.vnfTypeName = vnfTypeName;
		this.maxInputTrafficPerVnfInstance_Gbps = maxInputTrafficPerVnfInstance_Gbps;
		this.occupCpu = occupCpu;
		this.occupRamGB = occupRam;
		this.occupHdGB = occupHd;
		this.isConstrainedToBeInstantiatedOnlyInUserDefinedNodes = isConstrained;
		this.validMetroNodesForInstantiation = validMetroNodesForInstantiation;
		this.arbitraryParamString = arbitraryParamString;
	}
	
	/** Returns the arbitrary user-defined string attached to this VNF type 
	 * @return
	 */
	public String getArbitraryParamString()
	{
		return arbitraryParamString;
	}
	/** Sets the arbitrary user-defined string attached to this VNF type 
	 * @param arbitraryParamString
	 */
	public void setArbitraryParamString(String arbitraryParamString)
	{
		this.arbitraryParamString = arbitraryParamString;
	}
	/** Returns the type name
	 * @return
	 */
	public String getVnfTypeName()
	{
		return vnfTypeName;
	}
	/** Returns the maximum input traffic per VNF instance in Gbps
	 * @return
	 */
	public double getMaxInputTrafficPerVnfInstance_Gbps()
	{
		return maxInputTrafficPerVnfInstance_Gbps;
	}
	/** Returns the CPU occupation of each VNF instance of this type
	 * @return
	 */
	public double getOccupCpu()
	{
		return occupCpu;
	}
	/** Returns the RAM occupation of each VNF instance of this type in giga bytes
	 * @return
	 */
	public double getOccupRamGBytes()
	{
		return occupRamGB;
	}
	/** Returns the hard disk occupation of each VNF instance of this type in giga bytes
	 * @return
	 */
	public double getOccupHdGBytes()
	{
		return occupHdGB;
	}
	/** Indicates if this VNF type is constrained so instances can only be instantiated in some user-defined nodes
	 * @return
	 */
	public boolean isConstrainedToBeInstantiatedOnlyInUserDefinedNodes()
	{
		return isConstrainedToBeInstantiatedOnlyInUserDefinedNodes;
	}
	/** Returns the user-defined set of node names, so that instances of this VNF type can only be instantiated in those nodes (applicable only when constrained instantiation 
	 * is activated for this VNF type)
	 * @return
	 */
	public SortedSet<String> getValidMetroNodesForInstantiation()
	{
		return validMetroNodesForInstantiation;
	}
	
}
