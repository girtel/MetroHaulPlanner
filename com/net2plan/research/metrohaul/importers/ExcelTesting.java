package com.net2plan.research.metrohaul.importers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExcelTesting {
	
	public static void main(String[]args){

		File file = new File("MetroNetwork_v3.xlsx");		
		ImportMetroNetwork.importFromExcelFile(file);	
	}
}
