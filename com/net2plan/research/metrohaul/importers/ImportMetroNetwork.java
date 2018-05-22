package com.net2plan.research.metrohaul.importers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.research.metrohaul.importers.ExcelImporterConstants.COLUMNS_FIBERSTAB;
import com.net2plan.research.metrohaul.importers.ExcelImporterConstants.COLUMNS_NODESTAB;
import com.net2plan.research.metrohaul.importers.ExcelImporterConstants.COLUMNS_PERNODEANDSERVICETIMEINTENSITYGBPS;
import com.net2plan.research.metrohaul.importers.ExcelImporterConstants.COLUMNS_USERSERVICES;
import com.net2plan.research.metrohaul.importers.ExcelImporterConstants.COLUMNS_VNFTYPES;
import com.net2plan.research.metrohaul.networkModel.WFiber;
import com.net2plan.research.metrohaul.networkModel.WNet;
import com.net2plan.research.metrohaul.networkModel.WNetConstants;
import com.net2plan.research.metrohaul.networkModel.WNode;
import com.net2plan.research.metrohaul.networkModel.WServiceChainRequest;
import com.net2plan.research.metrohaul.networkModel.WUserService;
import com.net2plan.research.metrohaul.networkModel.WVnfType;
import com.net2plan.utils.Pair;
import com.net2plan.utils.Quadruple;
import com.net2plan.utils.Triple;

public class ImportMetroNetwork
{
	public static WNet importFromExcelFile (File excelFile)
    {
        final SortedMap<String, Object[][]> fileData = new TreeMap<>(ExcelReader.readFile(excelFile));
        final WNet net = WNet.createEmptyDesign ();
        
        /* Nodes sheet */
        System.out.println("Reading Nodes sheet");
        Object[][] sheet = fileData.get(ExcelImporterConstants.EXCELSHEETS.NODES.getTabName());
        if (sheet == null) throw new Net2PlanException ("Cannot read the excel sheet");
        System.out.println("Number of rows: "+sheet.length);
        for (int i = 1; i < sheet.length; i++)
        {
        	final Object[] thisRowData = sheet [i];
        	final String name = readString (thisRowData , COLUMNS_NODESTAB.NODEUNIQUENAME.ordinal());
        	System.out.println("Name loaded in sheet "+i+": "+name);
        	final String type = readString (thisRowData , COLUMNS_NODESTAB.NODETYPESTRING.ordinal());
        	System.out.println("Type loaded in sheet "+i+": "+type);
        	final double xCoord = readDouble (thisRowData , COLUMNS_NODESTAB.POSITIONLONGITUDE_DEGREEES.ordinal());
        	System.out.println("xCoord loaded in sheet "+i+": "+xCoord);
        	final double yCoord = readDouble (thisRowData , COLUMNS_NODESTAB.POSITIONLATITUDE_DEGREES.ordinal());
        	System.out.println("yCoord loaded in sheet "+i+": "+yCoord);
        	final boolean isConnectedToCoreNode = readBoolean(thisRowData, COLUMNS_NODESTAB.ISCONNECTEDTOCORENODE.ordinal()); 
        	System.out.println("isConnectedToCoreNode loaded in sheet "+i+": "+isConnectedToCoreNode);
        	final double nodeBasePopulation = readDouble (thisRowData , COLUMNS_NODESTAB.NODEBASEPOPULATION.ordinal());
        	System.out.println("nodeBasePopulation loaded in sheet "+i+": "+nodeBasePopulation);
        	final double nodeCpus = readDouble (thisRowData , COLUMNS_NODESTAB.TOTALNUMCPUS.ordinal());
        	System.out.println("nodeCpus loaded in sheet "+i+": "+nodeCpus);
        	final double nodeRamGb = readDouble (thisRowData , COLUMNS_NODESTAB.TOTALRAM_GB.ordinal());
        	System.out.println("nodeRamGb loaded in sheet "+i+": "+nodeRamGb);
        	final double nodeHdGb = readDouble (thisRowData , COLUMNS_NODESTAB.TOTALHD_GB.ordinal());
        	System.out.println("nodeHdGb loaded in sheet "+i+": "+nodeHdGb);
        	final String arbitraryParamsString = readString (thisRowData , COLUMNS_NODESTAB.ARBITRARYPARAMS.ordinal());
        	System.out.println("arbitraryParamsString loaded in sheet "+i+": "+arbitraryParamsString);
        	final WNode n = net.addNode(xCoord, yCoord, name, type);
        	n.setIsConnectedToNetworkCore(isConnectedToCoreNode);
        	n.setPoputlation(nodeBasePopulation);
        	n.setTotalNumCpus(nodeCpus);
        	n.setTotalRamGB(nodeRamGb);
        	n.setTotalHdGB(nodeHdGb);
        	n.setArbitraryParamString(arbitraryParamsString);
        }
        

        /* Fibers sheet */
        System.out.println("Reading Fibers sheet");
        sheet = fileData.get(ExcelImporterConstants.EXCELSHEETS.FIBERS.getTabName());
        if (sheet == null) throw new Net2PlanException ("Cannot read the excel sheet");
        for (int i = 1; i < sheet.length; i++)
        {
        	final Object[] thisRowData = sheet [i];
        	final String ORIGINNODEUNIQUENAME = readString (thisRowData , COLUMNS_FIBERSTAB.ORIGINNODEUNIQUENAME.ordinal());
        	final String DESTINATIONNODEUNIQUENAME = readString (thisRowData , COLUMNS_FIBERSTAB.DESTINATIONNODEUNIQUENAME.ordinal());
        	final double LENGTH_KM = readDouble (thisRowData , COLUMNS_FIBERSTAB.LENGTH_KM.ordinal());
        	final boolean ISBIDIRECTIONAL = readBoolean(thisRowData, COLUMNS_FIBERSTAB.ISBIDIRECTIONAL.ordinal()); 
        	final List<Double> VALIDOPTICALSLOTRANGES = readDoubleList(thisRowData , COLUMNS_FIBERSTAB.VALIDOPTICALSLOTRANGES.ordinal() , WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER);

        	final double FIBERATTENUATIONCOEFFICIENT_DBPERKM = readDouble (thisRowData , COLUMNS_FIBERSTAB.FIBERATTENUATIONCOEFFICIENT_DBPERKM.ordinal());
        	final double FIBERCHROMATICDISPERSIONCOEFFICIENT_PSPERNMPERKM = readDouble (thisRowData , COLUMNS_FIBERSTAB.FIBERCHROMATICDISPERSIONCOEFFICIENT_PSPERNMPERKM.ordinal());
        	final double FIBERLINKDESIGNVALUEPMD_PSPERSQRKM = readDouble (thisRowData , COLUMNS_FIBERSTAB.FIBERLINKDESIGNVALUEPMD_PSPERSQRKM.ordinal());
        	final List<Double> AMPLIFIERSPOSITIONFROMORIGIN_KM = readDoubleList(thisRowData , COLUMNS_FIBERSTAB.AMPLIFIERSPOSITIONFROMORIGIN_KM.ordinal() , WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER);
        	final List<Double> AMPLIFIERGAINS_DB = readDoubleList(thisRowData , COLUMNS_FIBERSTAB.AMPLIFIERGAINS_DB.ordinal() , WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER);
        	final List<Double> AMPLIFIERPMD_PS = readDoubleList(thisRowData , COLUMNS_FIBERSTAB.AMPLIFIERPMD_PS.ordinal() , WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER);
        	final String arbitraryParamsString = readString (thisRowData , COLUMNS_FIBERSTAB.ARBITRARYPARAMS.ordinal());
        	final WNode a = net.getNodeByName(ORIGINNODEUNIQUENAME).orElseThrow(()->new Net2PlanException ("Unkown node name: " + ORIGINNODEUNIQUENAME));
        	final WNode b = net.getNodeByName(DESTINATIONNODEUNIQUENAME).orElseThrow(()->new Net2PlanException ("Unkown node name: " + DESTINATIONNODEUNIQUENAME));
        	final List<Integer> validOpticalSlotRanges = VALIDOPTICALSLOTRANGES.stream().map(d->d.intValue()).collect(Collectors.toList());
        	final Pair<WFiber,WFiber> pair = net.addFiber(a, b, validOpticalSlotRanges, LENGTH_KM, ISBIDIRECTIONAL);
        	for (WFiber e : Arrays.asList(pair.getFirst() , pair.getSecond()))
        	{
        		if (e == null) continue;
            	e.setAttenuationCoefficient_dbPerKm(FIBERATTENUATIONCOEFFICIENT_DBPERKM);
            	e.setChromaticDispersionCoeff_psPerNmKm(FIBERCHROMATICDISPERSIONCOEFFICIENT_PSPERNMPERKM);
            	e.setPmdLinkDesignValueCoeff_psPerSqrKm(FIBERLINKDESIGNVALUEPMD_PSPERSQRKM);
            	e.setAmplifierPositionsKmFromOrigin_km(AMPLIFIERSPOSITIONFROMORIGIN_KM);
            	e.setAmplifierGains_dB(AMPLIFIERGAINS_DB);
            	e.setAmplifierPmd_ps(AMPLIFIERPMD_PS);
            	e.setArbitraryParamString(arbitraryParamsString);
        	}
        }
        
        /* VNF types sheet */
        System.out.println("Reading VNF types sheet");
        sheet = fileData.get(ExcelImporterConstants.EXCELSHEETS.VNFTYPES.getTabName());
        if (sheet == null) throw new Net2PlanException ("Cannot read the excel sheet");
        for (int i = 1; i < sheet.length; i++)
        {
        	final Object[] thisRowData = sheet [i];
        	final String VNFTYPEUNIQUENAME = readString (thisRowData , COLUMNS_VNFTYPES.VNFTYPEUNIQUENAME.ordinal());
        	final double VNFINSTANCECAPACITY_GBPS = readDouble (thisRowData , COLUMNS_VNFTYPES.VNFINSTANCECAPACITY_GBPS.ordinal());
        	final double OCCUPCPU = readDouble (thisRowData , COLUMNS_VNFTYPES.OCCUPCPU.ordinal());
        	final double OCCUPRAM_GB = readDouble (thisRowData , COLUMNS_VNFTYPES.OCCUPRAM_GB.ordinal());
        	final double OCCUPHD_GB = readDouble (thisRowData , COLUMNS_VNFTYPES.OCCUPHD_GB.ordinal());
        	final boolean ISCONSTRAINEDITSPLACEMENTTOSOMENODES = readBoolean(thisRowData, COLUMNS_VNFTYPES.ISCONSTRAINEDITSPLACEMENTTOSOMENODES.ordinal()); 
        	final List<String> LISTUNIQUENODENAMESOFNODESVALIDFORINSTANTIATION = readStringList(thisRowData , COLUMNS_VNFTYPES.LISTUNIQUENODENAMESOFNODESVALIDFORINSTANTIATION.ordinal() , WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER);
        	final String arbitraryParamsString = readString (thisRowData , COLUMNS_VNFTYPES.ARBITRARYPARAMS.ordinal());
        	final WVnfType vnfType = new WVnfType(VNFTYPEUNIQUENAME, 
        			VNFINSTANCECAPACITY_GBPS, 
        					OCCUPCPU, OCCUPRAM_GB, OCCUPHD_GB, 
        					ISCONSTRAINEDITSPLACEMENTTOSOMENODES, 
        					new TreeSet<> (LISTUNIQUENODENAMESOFNODESVALIDFORINSTANTIATION), 
        			arbitraryParamsString);
        	net.addOrUpdateVnfType(vnfType);
        }

        /* UserService sheet */
        System.out.println("Reading UserService sheet");
        sheet = fileData.get(ExcelImporterConstants.EXCELSHEETS.USERSERVICES.getTabName());
        if (sheet == null) throw new Net2PlanException ("Cannot read the excel sheet");
        for (int i = 1; i < sheet.length; i++)
        {
        	final Object[] thisRowData = sheet [i];
        	final String UNIQUEIDSTRING = readString (thisRowData , COLUMNS_USERSERVICES.UNIQUEIDSTRING.ordinal());
        	final List<String> LISTVNFTYPESCOMMASEPARATED_UPSTREAM = readStringList(thisRowData , COLUMNS_USERSERVICES.LISTVNFTYPESCOMMASEPARATED_UPSTREAM.ordinal() , WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER);
        	final List<String> LISTVNFTYPESCOMMASEPARATED_DOWNSTREAM = readStringList(thisRowData , COLUMNS_USERSERVICES.LISTVNFTYPESCOMMASEPARATED_DOWNSTREAM.ordinal() , WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER);
        	final List<Double> SEQUENCETRAFFICEXPANSIONFACTORRESPECTTOINITIAL_UPSTREAM = readDoubleList(thisRowData , COLUMNS_USERSERVICES.SEQUENCETRAFFICEXPANSIONFACTORRESPECTTOINITIAL_UPSTREAM.ordinal() , WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER);
        	final List<Double> SEQUENCETRAFFICEXPANSIONFACTORRESPECTTOINITIAL_DOWNSTREAM = readDoubleList(thisRowData , COLUMNS_USERSERVICES.SEQUENCETRAFFICEXPANSIONFACTORRESPECTTOINITIAL_DOWNSTREAM.ordinal() , WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER);
        	final List<Double> LISTMAXLATENCYFROMINITIALTOVNFSTART_MS_UPSTREAM = readDoubleList(thisRowData , COLUMNS_USERSERVICES.LISTMAXLATENCYFROMINITIALTOVNFSTART_MS_UPSTREAM.ordinal() , WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER);
        	final List<Double> LISTMAXLATENCYFROMINITIALTOVNFSTART_MS_DOWNSTREAM = readDoubleList(thisRowData , COLUMNS_USERSERVICES.LISTMAXLATENCYFROMINITIALTOVNFSTART_MS_DOWNSTREAM.ordinal() , WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER);
        	final double INJECTIONDOWNSTREAMEXPANSIONFACTORRESPECTTOINITIALUPSTREAM = readDouble (thisRowData , COLUMNS_USERSERVICES.INJECTIONDOWNSTREAMEXPANSIONFACTORRESPECTTOINITIALUPSTREAM.ordinal());
        	final boolean ISENDINGINCORENODE = readBoolean(thisRowData, COLUMNS_USERSERVICES.ISENDINGINCORENODE.ordinal()); 
        	final String arbitraryParamString = readString (thisRowData , COLUMNS_USERSERVICES.ARBITRARYPARAMS.ordinal());
        	final WUserService userService = new WUserService(UNIQUEIDSTRING, LISTVNFTYPESCOMMASEPARATED_UPSTREAM,
        			LISTVNFTYPESCOMMASEPARATED_DOWNSTREAM,
        			SEQUENCETRAFFICEXPANSIONFACTORRESPECTTOINITIAL_UPSTREAM,
        			SEQUENCETRAFFICEXPANSIONFACTORRESPECTTOINITIAL_DOWNSTREAM,
        			LISTMAXLATENCYFROMINITIALTOVNFSTART_MS_UPSTREAM,
        			LISTMAXLATENCYFROMINITIALTOVNFSTART_MS_DOWNSTREAM,
        			INJECTIONDOWNSTREAMEXPANSIONFACTORRESPECTTOINITIALUPSTREAM, 
        			ISENDINGINCORENODE , 
        			arbitraryParamString);
        	net.addOrUpdateUserService(userService);	
        }

        /* Per node and service time intensity sheet */
        sheet = fileData.get(ExcelImporterConstants.EXCELSHEETS.PERNODEANDSERVICETIMETRAFFIC.getTabName());
        if (sheet == null) throw new Net2PlanException ("Cannot read the excel sheet");
        for (int i = 1; i < sheet.length; i++)
        {
        	final Object[] thisRowData = sheet [i];
        	final String serviceChainInjectionNodeUniqueName = readString (thisRowData , COLUMNS_PERNODEANDSERVICETIMEINTENSITYGBPS.INJECTIONNODEUIQUENAME.ordinal());
        	final String serviceChainUserServiceUniqueName = readString (thisRowData , COLUMNS_PERNODEANDSERVICETIMEINTENSITYGBPS.USERSERVICEUNIQUEID.ordinal());
        	final WUserService userService = net.getUserServicesInfo().getOrDefault(serviceChainUserServiceUniqueName, null);
        	final WNode userInjectionNode = net.getNodeByName(serviceChainInjectionNodeUniqueName).orElse(null);
        	if (userService == null || userInjectionNode == null) { System.out.println("Not readable row: " + serviceChainInjectionNodeUniqueName + " ; " + serviceChainUserServiceUniqueName); continue; }
        	final List<Pair<String,Double>> intervalNameAndTrafficUpstream_Gbps = new ArrayList<> ();
        	for (int col = 2 ; col < thisRowData.length ; col ++)
        	{
            	final String timeSlotName = readString (sheet [0] , COLUMNS_PERNODEANDSERVICETIMEINTENSITYGBPS.USERSERVICEUNIQUEID.ordinal() , "");
            	final Double trafficUpstreamInitialGbps = readDouble(thisRowData , col , 0.0);
            	intervalNameAndTrafficUpstream_Gbps.add(Pair.of(timeSlotName, trafficUpstreamInitialGbps));
        	}
        	final WServiceChainRequest upstreamScReq = net.addServiceChainRequest(userInjectionNode, true, userService);
        	upstreamScReq.setFullTrafficIntensityInfo(intervalNameAndTrafficUpstream_Gbps);
        	final double injectionDownstreamExpansionFactorRespecToBaseTrafficUpstream = userService.getInjectionDownstreamExpansionFactorRespecToBaseTrafficUpstream(); 
        	if (injectionDownstreamExpansionFactorRespecToBaseTrafficUpstream > 0)
        	{
            	final WServiceChainRequest downstreamScReq = net.addServiceChainRequest(userInjectionNode, false, userService);
            	final List<Pair<String,Double>> intervalNameAndTrafficDownstream_Gbps = intervalNameAndTrafficUpstream_Gbps.stream().map(p->Pair.of(p.getFirst(), injectionDownstreamExpansionFactorRespecToBaseTrafficUpstream * p.getSecond())).collect(Collectors.toList());
            	downstreamScReq.setFullTrafficIntensityInfo(intervalNameAndTrafficDownstream_Gbps);
        	}
        }
        return net;
    }

	
	private static double readDouble (Object [] cells , int index , Double...defaultVal)
	{
		if (index >= cells.length) throw new Net2PlanException ("Unexisting cell of column: " + index + ". Num columns in this row: " + cells.length);
		if (cells [index] == null) if (defaultVal.length > 0) return defaultVal[0]; else throw new Net2PlanException ("Cell unkown instance " + (cells[index]).getClass().getName());
		if (cells [index] instanceof Number) return ((Number) cells[index]).doubleValue();
		if (cells [index] instanceof String) return Double.parseDouble((String) cells[index]);
		if (defaultVal.length > 0) return defaultVal[0]; else throw new Net2PlanException ("Cell unkown instance " + (cells[index]).getClass().getName());	
	}
	private static int readInt (Object [] cells , int index)
	{
		if (index >= cells.length) throw new Net2PlanException ("Unexisting cell of column: " + index + ". Num columns in this row: " + cells.length);
		if (cells [index] == null) return 0;
		if (cells [index] instanceof Number) return ((Number) cells[index]).intValue();
		if (cells [index] instanceof String) return Integer.parseInt((String) cells[index]);
		throw new Net2PlanException ("Cell unkown instance " + (cells[index]).getClass().getName());
	}
	private static String readString (Object [] cells , int index , String... defaultVal)
	{
		if (index >= cells.length) throw new Net2PlanException ("Unexisting cell of column: " + index + ". Num columns in this row: " + cells.length);
		if (cells [index] == null) if (defaultVal.length > 0) return defaultVal[0]; else throw new Net2PlanException("Cell unkown instance " + (cells[index]).getClass().getName());
		if (cells [index] instanceof Number) return ((Number) cells[index]).toString();
		if (cells [index] instanceof String) return (String) cells[index];
		if (defaultVal.length > 0) return defaultVal[0]; else throw new Net2PlanException("Cell unkown instance " + (cells[index]).getClass().getName());	}
	private static boolean readBoolean (Object [] cells , int index)
	{
		return readDouble (cells , index) != 0;
	}
	private static List<Double> readDoubleList (Object [] cells , int index , String separator)
	{
		final String st = readString (cells , index);
		return Arrays.asList(st.split(separator)).stream().map(s->s.trim()).map(s->Double.parseDouble(s)).collect(Collectors.toCollection(ArrayList::new));
	}
	private static List<String> readStringList (Object [] cells , int index , String separator)
	{
		final String st = readString (cells , index);
		return Arrays.asList(st.split(separator)).stream().map(s->s.trim()).collect(Collectors.toCollection(ArrayList::new));
	}

}

