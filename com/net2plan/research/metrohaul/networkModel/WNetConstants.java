package com.net2plan.research.metrohaul.networkModel;

import java.util.Arrays;
import java.util.List;

public class WNetConstants
{
	/**
	 * The central frequency of optical slot 0. The central frequency of slot i is given by 193.1 + 0.0125 i THz 
	 */
	public static final double CENTRALFREQUENCYOFOPTICALSLOTZERO_THZ = 193.1;
	/**
	 * The optical slot size in GHz 
	 */
	public static final double OPTICALSLOTSIZE_GHZ = 12.5;

	
	static final String WNODE_NAMEOFANYCASTORIGINNODE = "ANYCASTORIGIN" + WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER;
	static final String WNODE_NAMEOFANYCASTDESTINATION = "ANYCASTDESTINATION" + WNetConstants.LISTSEPARATORANDINVALIDNAMECHARACTER;
	static final String LISTSEPARATORANDINVALIDNAMECHARACTER = ",";
	
	static final boolean WNODE_DEFAULT_ISCONNECTEDTOCORE = false;
	static final double WFIBER_DEFAULT_ATTCOEFFICIENTDBPERKM = 0.25;
	static final double WFIBER_DEFAULT_PMDCOEFF_PSPERSQRKM = 0.5;
	static final double WFIBER_DEFAULT_CDCOEFF_PSPERNMKM = 15;
	static final double WFIBER_DEFAULT_AMPLIFIERGAIN_DB = 15;
	static final double WFIBER_DEFAULT_AMPLIFIERPMD_PS = 15;
	static final double WFIBER_DEFAULT_AMPLIFIERNOISEFACTOR_DB = 6;
	
	static final double WFIBER_DEFAULT_PROPAGATIONSPEEDKMPERSEC = 200000;
	static final List<Double> WFIBER_DEFAULT_VALIDOPTICALSLOTRANGES = Arrays.asList(1.0,320.0);
	static final boolean WLPREQUEST_DEFAULT_ISTOBE11PROTECTED = false;
	static final boolean WSERVICECHAINREQUEST_DEFAULT_ISENDEDINCORENODE = false;
	
}
