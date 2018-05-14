package com.net2plan.research.metrohaul.importers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Sheet;

import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.research.metrohaul.importers.ExcelImporterConstants.COLUMNS_FIBERSTAB;
import com.net2plan.research.metrohaul.importers.ExcelImporterConstants.COLUMNS_NODESTAB;
import com.net2plan.research.metrohaul.importers.ExcelImporterConstants.COLUMNS_VNFTYPES;
import com.net2plan.research.metrohaul.networkModel.WFiber;
import com.net2plan.research.metrohaul.networkModel.WNet;
import com.net2plan.research.metrohaul.networkModel.WNetConstants;
import com.net2plan.research.metrohaul.networkModel.WNode;
import com.net2plan.research.metrohaul.networkModel.WVnfType;
import com.net2plan.utils.Pair;
import com.net2plan.utils.Quadruple;
import com.net2plan.utils.Triple;

public class ImportMetroNetwork
{
	public WNet importFromExcelFile (File excelFile)
    {
        final SortedMap<String, Object[][]> fileData = new TreeMap<>(ExcelReader.readFile(excelFile));
        final WNet net = new WNet (new NetPlan ());
        
        /* Nodes sheet */
        Object[][] sheet = fileData.get(ExcelImporterConstants.EXCELSHEETS.NODES.getTabName());
        if (sheet == null) throw new Net2PlanException ("Cannot read the excel sheet");
        for (int i = 1; i < sheet.length; i++)
        {
        	final Object[] thisRowData = sheet [i];
        	final String name = readString (thisRowData , COLUMNS_NODESTAB.NODEUNIQUENAME.getIndex());
        	final String type = readString (thisRowData , COLUMNS_NODESTAB.NODETYPESTRING.getIndex());
        	final double xCoord = readDouble (thisRowData , COLUMNS_NODESTAB.POSITIONLONGITUDE_DEGREEES.getIndex());
        	final double yCoord = readDouble (thisRowData , COLUMNS_NODESTAB.POSITIONLATITUDE_DEGREES.getIndex());
        	final boolean isConnectedToCoreNode = readBoolean(thisRowData, COLUMNS_NODESTAB.ISCONNECTEDTOCORENODE.getIndex()); 
        	final double nodeBasePopulation = readDouble (thisRowData , COLUMNS_NODESTAB.NODEBASEPOPULATION.getIndex());
        	final double nodeCpus = readDouble (thisRowData , COLUMNS_NODESTAB.TOTALNUMCPUS.getIndex());
        	final double nodeRamGb = readDouble (thisRowData , COLUMNS_NODESTAB.TOTALRAM_GB.getIndex());
        	final double nodeHdGb = readDouble (thisRowData , COLUMNS_NODESTAB.TOTALHD_GB.getIndex());
        	final String arbitraryParamsString = readString (thisRowData , COLUMNS_NODESTAB.ARBITRARYPARAMS.getIndex());
        	final WNode n = net.addNode(xCoord, yCoord, name, type);
        	n.setIsConnectedToNetworkCore(isConnectedToCoreNode);
        	n.setPoputlation(nodeBasePopulation);
        	n.setTotalNumCpus(nodeCpus);
        	n.setTotalRamGB(nodeRamGb);
        	n.setTotalHdGB(nodeHdGb);
        	n.setArbitraryParamString(arbitraryParamsString);
        }

        /* Fibers sheet */
        sheet = fileData.get(ExcelImporterConstants.EXCELSHEETS.FIBERS.getTabName());
        if (sheet == null) throw new Net2PlanException ("Cannot read the excel sheet");
        for (int i = 1; i < sheet.length; i++)
        {
        	final Object[] thisRowData = sheet [i];
        	final String ORIGINNODEUNIQUENAME = readString (thisRowData , COLUMNS_FIBERSTAB.ORIGINNODEUNIQUENAME.getIndex());
        	final String DESTINATIONNODEUNIQUENAME = readString (thisRowData , COLUMNS_FIBERSTAB.DESTINATIONNODEUNIQUENAME.getIndex());
        	final double LENGTH_KM = readDouble (thisRowData , COLUMNS_FIBERSTAB.LENGTH_KM.getIndex());
        	final boolean ISBIDIRECTIONAL = readBoolean(thisRowData, COLUMNS_FIBERSTAB.ISBIDIRECTIONAL.getIndex()); 
        	final List<Double> VALIDOPTICALSLOTRANGES = readDoubleList(thisRowData , COLUMNS_FIBERSTAB.VALIDOPTICALSLOTRANGES.getIndex() , WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER);

        	final double FIBERATTENUATIONCOEFFICIENT_DBPERKM = readDouble (thisRowData , COLUMNS_FIBERSTAB.FIBERATTENUATIONCOEFFICIENT_DBPERKM.getIndex());
        	final double FIBERCHROMATICDISPERSIONCOEFFICIENT_PSPERNMPERKM = readDouble (thisRowData , COLUMNS_FIBERSTAB.FIBERCHROMATICDISPERSIONCOEFFICIENT_PSPERNMPERKM.getIndex());
        	final double FIBERLINKDESIGNVALUEPMD_PSPERSQRKM = readDouble (thisRowData , COLUMNS_FIBERSTAB.FIBERLINKDESIGNVALUEPMD_PSPERSQRKM.getIndex());
        	final List<Double> AMPLIFIERSPOSITIONFROMORIGIN_KM = readDoubleList(thisRowData , COLUMNS_FIBERSTAB.AMPLIFIERSPOSITIONFROMORIGIN_KM.getIndex() , WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER);
        	final List<Double> AMPLIFIERGAINS_DB = readDoubleList(thisRowData , COLUMNS_FIBERSTAB.AMPLIFIERGAINS_DB.getIndex() , WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER);
        	final List<Double> AMPLIFIERPMD_PS = readDoubleList(thisRowData , COLUMNS_FIBERSTAB.AMPLIFIERPMD_PS.getIndex() , WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER);
        	final String arbitraryParamsString = readString (thisRowData , COLUMNS_FIBERSTAB.ARBITRARYPARAMS.getIndex());
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
        sheet = fileData.get(ExcelImporterConstants.EXCELSHEETS.VNFTYPES.getTabName());
        if (sheet == null) throw new Net2PlanException ("Cannot read the excel sheet");
        for (int i = 1; i < sheet.length; i++)
        {
        	final Object[] thisRowData = sheet [i];
        	final String VNFTYPEUNIQUENAME = readString (thisRowData , COLUMNS_VNFTYPES.VNFTYPEUNIQUENAME.getIndex());
        	final double VNFINSTANCECAPACITY_GBPS = readDouble (thisRowData , COLUMNS_VNFTYPES.VNFINSTANCECAPACITY_GBPS.getIndex());
        	final double OCCUPCPU = readDouble (thisRowData , COLUMNS_VNFTYPES.OCCUPCPU.getIndex());
        	final double OCCUPRAM_GB = readDouble (thisRowData , COLUMNS_VNFTYPES.OCCUPRAM_GB.getIndex());
        	final double OCCUPHD_GB = readDouble (thisRowData , COLUMNS_VNFTYPES.OCCUPHD_GB.getIndex());
        	final boolean ISCONSTRAINEDITSPLACEMENTTOSOMENODES = readBoolean(thisRowData, COLUMNS_VNFTYPES.ISCONSTRAINEDITSPLACEMENTTOSOMENODES.getIndex()); 
        	final List<String> LISTUNIQUENODENAMESOFNODESVALIDFORINSTANTIATION = readStringList(thisRowData , COLUMNS_VNFTYPES.LISTUNIQUENODENAMESOFNODESVALIDFORINSTANTIATION.getIndex() , WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER);
        	final String arbitraryParamsString = readString (thisRowData , COLUMNS_VNFTYPES.ARBITRARYPARAMS.getIndex());
        	final WVnfType vnfType = new WVnfType(VNFTYPEUNIQUENAME, 
        			Quadruple.of(VNFINSTANCECAPACITY_GBPS, 
        					Triple.of(OCCUPCPU, OCCUPRAM_GB, OCCUPHD_GB), 
        					ISCONSTRAINEDITSPLACEMENTTOSOMENODES, 
        					new TreeSet<> (LISTUNIQUENODENAMESOFNODESVALIDFORINSTANTIATION)), 
        			arbitraryParamsString);
        	net.addOrUpdateVnfType(VNFTYPEUNIQUENAME, vnfType);
        }

        /* VNF types sheet */
        PABLO: CONTINUA CON EL USERS Sheet. 
        ELIMINAR CLASE WUSERSERVICELIST -- METER NET WNET
        QUE AL CONSTRUCTOR SE LE PASEN STRINGS, NO OBJETOS. TENER METODO GET PARA STRING, Y GET PARA OBJETOS PARA FACILITAR.
        
        
        return net;
    }

	
	private static double readDouble (Object [] cells , int index)
	{
		if (index >= cells.length) throw new Net2PlanException ("Unexisting cell of column: " + index + ". Num columns in this row: " + cells.length);
		if (cells [index] == null) return 0;
		if (cells [index] instanceof Number) return ((Number) cells[index]).doubleValue();
		if (cells [index] instanceof String) return Double.parseDouble((String) cells[index]);
		throw new Net2PlanException ("Cell unkown instance " + (cells[index]).getClass().getName());
	}
	private static int readInt (Object [] cells , int index)
	{
		if (index >= cells.length) throw new Net2PlanException ("Unexisting cell of column: " + index + ". Num columns in this row: " + cells.length);
		if (cells [index] == null) return 0;
		if (cells [index] instanceof Number) return ((Number) cells[index]).intValue();
		if (cells [index] instanceof String) return Integer.parseInt((String) cells[index]);
		throw new Net2PlanException ("Cell unkown instance " + (cells[index]).getClass().getName());
	}
	private static String readString (Object [] cells , int index)
	{
		if (index >= cells.length) throw new Net2PlanException ("Unexisting cell of column: " + index + ". Num columns in this row: " + cells.length);
		if (cells [index] == null) return "";
		if (cells [index] instanceof Number) return ((Number) cells[index]).toString();
		if (cells [index] instanceof String) return (String) cells[index];
		throw new Net2PlanException ("Cell unkown instance " + (cells[index]).getClass().getName());
	}
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
