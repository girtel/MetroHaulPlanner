package com.net2plan.research.metrohaul.importers;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.research.metrohaul.networkModel.*;
import com.net2plan.utils.Triple;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ExcelTest implements IAlgorithm
{

	@Override
	public String executeAlgorithm(NetPlan netPlan, Map<String, String> algorithmParameters, Map<String, String> net2planParameters)
	{
		final String pathExcel = algorithmParameters.get("pathExcel");	
		File file = new File(pathExcel);	
		WNet net = ImportMetroNetwork.importFromExcelFile(file);		
		netPlan.copyFrom(net.getNe());
		
		return "Excel file has been loaded successfully";
	}

	@Override
	public String getDescription()
	{
		return "Test Excel file";
	}

	@Override
	public List<Triple<String, String, String>> getParameters()
	{
		final List<Triple<String, String, String>> param = new LinkedList<Triple<String, String, String>> ();

		param.add (Triple.of ("pathExcel" , "MetroNetwork_v3.xlsx" , "Excel file"));
		
		return param;
	}
}