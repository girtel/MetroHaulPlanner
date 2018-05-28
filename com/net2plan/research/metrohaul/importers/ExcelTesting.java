package com.net2plan.research.metrohaul.importers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExcelTesting {
	
	public static void main(String[]args){

		File file = new File("C:/Users/jlrg_/Documents/MetroHaulPlanner/MetroNetwork_v2.xlsx");		
		ImportMetroNetwork.importFromExcelFile(file);
//		ArrayList<String> al =  new ArrayList<String>();
//		System.out.println(al.size());
//		al.add("ei");
//		System.out.println(al.size());
		
	}
}
