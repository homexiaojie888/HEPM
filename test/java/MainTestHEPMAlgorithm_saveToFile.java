import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

public class MainTestHEPMAlgorithm_saveToFile {
	public static void main(String [] arg) throws IOException{

		String input = fileToPath("DB_Utility.txt");
		String input2 = fileToPath("stock.txt");
		String output = ".//output.txt";

		double minEffiency=0.3;

		UtilityTransactionDatabaseTP database = new UtilityTransactionDatabaseTP();
		Stock stock=new Stock();
		database.loadFile(input);
		stock.loadFile(input2);

		AlgoHEPM algoHEPM = new AlgoHEPM();
		ItemsetsTP highEfficiencyItemsets = algoHEPM.runAlgorithm(database,stock, minEffiency);
		highEfficiencyItemsets.saveResultsToFile(output, database.getTransactions().size());

		algoHEPM.printStats();

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestHEPMAlgorithm_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
