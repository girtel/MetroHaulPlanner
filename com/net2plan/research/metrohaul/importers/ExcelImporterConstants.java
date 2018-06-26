package com.net2plan.research.metrohaul.importers;

public class ExcelImporterConstants
{
	public enum EXCELSHEETS 
	{ 
		NODES ("Nodes"), FIBERS ("Fibers"), USERSERVICES("UserServices"), VNFTYPES ("VnfTypes") , PERNODEANDSERVICETIMETRAFFIC("PerNodeAndServiceTimeIntensity");  
		private final String tabName; 
		private EXCELSHEETS (String tabName) { this.tabName = tabName; }
		public String getTabName () { return tabName;}
	}
	public enum COLUMNS_NODESTAB 
	{
		NODEUNIQUENAME, 
		POSITIONLONGITUDE_DEGREEES, 
		POSITIONLATITUDE_DEGREES,
		NODETYPESTRING,
		ISCONNECTEDTOCORENODE, 
		NODEBASEPOPULATION,
		TOTALNUMCPUS,
		TOTALRAM_GB,
		TOTALHD_GB,
		ARBITRARYPARAMS;  
	}
	
	public enum COLUMNS_FIBERSTAB
	{
		ORIGINNODEUNIQUENAME, 
		DESTINATIONNODEUNIQUENAME,
		LENGTH_KM,
		ISBIDIRECTIONAL, 
		VALIDOPTICALSLOTRANGES,
		FIBERATTENUATIONCOEFFICIENT_DBPERKM,
		FIBERCHROMATICDISPERSIONCOEFFICIENT_PSPERNMPERKM,
		FIBERLINKDESIGNVALUEPMD_PSPERSQRKM,
		AMPLIFIERSPOSITIONFROMORIGIN_KM,
		AMPLIFIERGAINS_DB,
		AMPLIFIERPMD_PS,
		ARBITRARYPARAMS;  
	}

	public enum COLUMNS_VNFTYPES
	{
		VNFTYPEUNIQUENAME, 
		VNFINSTANCECAPACITY_GBPS, 
		OCCUPCPU,
		OCCUPRAM_GB,
		OCCUPHD_GB,
		ISCONSTRAINEDITSPLACEMENTTOSOMENODES, 
		LISTUNIQUENODENAMESOFNODESVALIDFORINSTANTIATION, 
		ARBITRARYPARAMS;  
	}

	public enum COLUMNS_USERSERVICES
	{
		UNIQUEIDSTRING, 
		LISTVNFTYPESCOMMASEPARATED_UPSTREAM, 
		LISTVNFTYPESCOMMASEPARATED_DOWNSTREAM,
		SEQUENCETRAFFICEXPANSIONFACTORRESPECTTOINITIAL_UPSTREAM,
		SEQUENCETRAFFICEXPANSIONFACTORRESPECTTOINITIAL_DOWNSTREAM,
		LISTMAXLATENCYFROMINITIALTOVNFSTART_MS_UPSTREAM, 
		LISTMAXLATENCYFROMINITIALTOVNFSTART_MS_DOWNSTREAM, 
		INJECTIONDOWNSTREAMEXPANSIONFACTORRESPECTTOINITIALUPSTREAM,
		ISENDINGINCORENODE,
		ARBITRARYPARAMS;  
	}

	public enum COLUMNS_PERNODEANDSERVICETIMEINTENSITYGBPS
	{
		INJECTIONNODEUIQUENAME, 
		USERSERVICEUNIQUEID;  
	}
	
}