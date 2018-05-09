package com.net2plan.research.metrohaul.networkModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.utils.Pair;
import com.net2plan.utils.Quadruple;
import com.net2plan.utils.Triple;

public class WVnfTypeList extends WAbstractNetworkElement
{
	private static final String ATTNAME_VNFTYPELIST = "VnfTypeListMatrix";
	private final NetPlan np;
	public WVnfTypeList (NetPlan np) { super (np); this.np = np; }
	
	public SortedMap<String , WVnfType> getVnfInfo ()
	{
		final SortedMap<String , WVnfType> res = new TreeMap<> ();
		final List<List<String>> matrix = getNe().getAttributeAsStringMatrix(ATTNAME_VNFTYPELIST, null);
		if (matrix == null) throw new Net2PlanException("Wrong format");
		for (List<String> row : matrix)
		{
			if (row.size() != 7) throw new Net2PlanException ("Wrong format"); 
			final String vnfTypeName = row.get(0);
			if (res.containsKey(vnfTypeName)) throw new Net2PlanException ("VNF type names must be unique");
			final double maxInputTraffic_Gbps = Double.parseDouble(row.get(1));
			final double numCpus = Double.parseDouble(row.get(2));
			final double numRam = Double.parseDouble(row.get(3));
			final double numHd = Double.parseDouble(row.get(4));
			final boolean isConstrained = Boolean.parseBoolean(row.get(5));
			final SortedSet<String> nodeNames = new TreeSet<> (Arrays.asList(row.get(6).split(" ")));
			final SortedSet<WNode> nodes = new TreeSet<> ();
			for (String nodeName : nodeNames)
				nodes.add(new WNet(np).getNodeByName(nodeName).orElseThrow(()->new Net2PlanException ("Unknown node name")));
			res.put(vnfTypeName, new WVnfType(vnfTypeName, Quadruple.of (maxInputTraffic_Gbps , Triple.of(numCpus, numRam, numHd) , isConstrained , nodes)));
		}
		return res;
	}

	public void addOrUpdateVnfInfo (String vnfTypeName , WVnfType info)
	{
		final SortedMap<String , WVnfType> newInfo = this.getVnfInfo();
		newInfo.put(vnfTypeName, info);
		this.setVnfInfo(newInfo);
	}
	
	public void removeVnfInfo (String vnfTypeName)
	{
		final SortedMap<String , WVnfType> newInfo = this.getVnfInfo();
		newInfo.remove(vnfTypeName);
		this.setVnfInfo(newInfo);
	}

	public void setVnfInfo (SortedMap<String , WVnfType> newInfo)
	{
		final List<List<String>> matrix = new ArrayList<> ();
		for (Entry<String , WVnfType> entry : newInfo.entrySet())
		{
			final List<String> infoThisVnf = new LinkedList<> ();
			infoThisVnf.add(entry.getKey());
			infoThisVnf.add(entry.getValue().getMaxInputTrafficPerVnfInstance_Gbps() + "");
			infoThisVnf.add(entry.getValue().getOccupCpu() + "");
			infoThisVnf.add(entry.getValue().getOccupRam() + "");
			infoThisVnf.add(entry.getValue().getOccupHd() + "");
			infoThisVnf.add(entry.getValue().isConstrained()? "1"  : "0");
			infoThisVnf.add(entry.getValue().getValidMetroNodesForInstantiation().stream().map(n->n.getName()).collect(Collectors.joining(" ")));
			matrix.add(infoThisVnf);
		}
		np.setAttributeAsStringMatrix(ATTNAME_VNFTYPELIST, matrix);
	}
	
	@Override
	public NetPlan getNe() { return np; }	
	
	public SortedSet<String> getVnfTypeNames () { return new TreeSet<> (getVnfInfo().keySet()); }
	
}
