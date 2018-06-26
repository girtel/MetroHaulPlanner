import com.net2plan.research.metrohaul.importers.ImportMetroNetwork;
import com.net2plan.research.metrohaul.networkModel.WNet;

import java.io.File;

public class ExcelTester
{
	public static void main(String[] args)
	{

		File file = new File("MetroNetwork_v3.xlsx");
		WNet wNet = ImportMetroNetwork.importFromExcelFile(file);
		wNet.saveToFile(new File("ExcelTest.n2p"));
	}
}
