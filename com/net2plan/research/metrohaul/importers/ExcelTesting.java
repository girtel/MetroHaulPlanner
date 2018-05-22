package com.net2plan.research.metrohaul.importers;

import java.io.File;
import java.util.Map;

public class ExcelTesting {
	
	public static void main(String[]args){

		File file = new File("C:/Users/jlrg_/Documents/MetroHaulPlanner/MetroNetwork_v2.xlsx");		
		ImportMetroNetwork iMN = new ImportMetroNetwork();
		iMN.importFromExcelFile(file);
		System.out.println("Done");
		
		
	}
}
