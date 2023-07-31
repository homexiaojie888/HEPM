import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

public class MainTestHEPMinerAlgorithm_saveToFile {
	public static void main(String [] arg) throws IOException{

		String input = fileToPath("DB_Utility.txt");
		String input2 = fileToPath("Stock.txt");
		String output = ".//output2.txt";
		double minEffiency=0.3; //
		Stock stock=new Stock();
		stock.loadFile(input2);
		// Applying the HUIMiner algorithm
		AlgoHEPMiner algoHEPMiner = new AlgoHEPMiner();
		algoHEPMiner.runAlgorithm(input, output, stock, minEffiency);
		algoHEPMiner.printStats();

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestHEPMinerAlgorithm_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
