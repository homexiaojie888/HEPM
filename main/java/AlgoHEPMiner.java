/* This file is copyright (c) 2008-2015 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
* 
*/


import java.io.*;
import java.util.*;
import java.util.Map.Entry;
/**
 * This is an implementation of the "HEPMiner" for High-Efficiency pattern Mining.
 * <br/><br/>
 * @see ItemsetsTP
 * @see ItemsetTP
 * @see TransactionTP
 * @author xiaojie Zhang
 */
public class AlgoHEPMiner {

	public long startTimestamp = 0;
	public long endTimestamp = 0;
	public int heiCount =0;
	private int joinCount;
	private int candidateCount;
	public double minEffiency;
	public Stock stock;
	Map<Integer, Long> mapItemToTWU;
	BufferedWriter writer = null;
	Map<Integer, Map<Integer, Long>> mapFMAP;
	boolean ENABLE_LA_PRUNE = true;
	boolean DEBUG = false;
	final int BUFFERS_SIZE = 200;
	private int[] itemsetBuffer = null;

	class Pair{
		int item = 0;
		int utility = 0;
	}

	public AlgoHEPMiner() {
		
	}

	/**
	 * Run the algorithm
	 * @param input the input file path
	 * @param output the output file path
	 * @param minEffiency the minimum utility threshold
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, Stock stock, double minEffiency) throws IOException {

		MemoryLogger.getInstance().reset();
		this.minEffiency=minEffiency;
		this.stock=stock;
		candidateCount=0;
		itemsetBuffer = new int[BUFFERS_SIZE];
		mapFMAP =  new HashMap<Integer, Map<Integer, Long>>();
		startTimestamp = System.currentTimeMillis();
		writer = new BufferedWriter(new FileWriter(output));
		mapItemToTWU = new HashMap<Integer, Long>();

		BufferedReader myInput = null;
		String thisLine;
		try {

			myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
			while ((thisLine = myInput.readLine()) != null) {
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}

				String split[] = thisLine.split(":");
				String items[] = split[0].split(" ");
				int transactionUtility = Integer.parseInt(split[1]);
				for(int i=0; i <items.length; i++){
					Integer item = Integer.parseInt(items[i]);
					Long twu = mapItemToTWU.get(item);
					twu = (twu == null)? 
							transactionUtility : twu + transactionUtility;
					mapItemToTWU.put(item, twu);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
		List<UtilityList> listOfUtilityLists = new ArrayList<UtilityList>();
		Map<Integer, UtilityList> mapItemToUtilityList = new HashMap<Integer, UtilityList>();
		for(Integer item: mapItemToTWU.keySet()){
			double estimateEfficiency=(double)mapItemToTWU.get(item)/stock.investMap.get(item);
			if(Tool.compare(estimateEfficiency, minEffiency) >= 0){
				UtilityList uList = new UtilityList(item,stock.investMap.get(item));
				mapItemToUtilityList.put(item, uList);
				listOfUtilityLists.add(uList);
			}
		}
		Collections.sort(listOfUtilityLists, new Comparator<UtilityList>(){
			public int compare(UtilityList o1, UtilityList o2) {
				return compareItems2(o1.item, o2.item);
			}
			} );
		
		// SECOND DATABASE PASS TO CONSTRUCT THE Efficiency LISTS
		// OF 1-ITEMSETS
		try {
			myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));
			int tid =0;
			while ((thisLine = myInput.readLine()) != null) {
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}

				String split[] = thisLine.split(":");
				String items[] = split[0].split(" ");
				String utilityValues[] = split[2].split(" ");
				int remainingUtility =0;
				long newTWU = 0;
				List<Pair> revisedTransaction = new ArrayList<Pair>();
				for(int i=0; i <items.length; i++){
					Pair pair = new Pair();
					pair.item = Integer.parseInt(items[i]);
					pair.utility = Integer.parseInt(utilityValues[i]);
					double es=(double)mapItemToTWU.get(pair.item)/stock.investMap.get(pair.item);
					if(Tool.compare(es,minEffiency) >= 0){
						revisedTransaction.add(pair);
						remainingUtility += pair.utility;
						newTWU += pair.utility;
					}
				}
				Collections.sort(revisedTransaction, new Comparator<Pair>(){
					public int compare(Pair o1, Pair o2) {
						return compareItems2(o1.item, o2.item);
					}});
					for(int i = 0; i< revisedTransaction.size(); i++){
						Pair pair =  revisedTransaction.get(i);
						remainingUtility = remainingUtility - pair.utility;
						UtilityList utilityListOfItem = mapItemToUtilityList.get(pair.item);
						if (utilityListOfItem==null){
							System.out.println(pair.item);
						}
						Element element = new Element(tid, pair.utility, remainingUtility);
						utilityListOfItem.addElement(element);
						Map<Integer, Long> mapFMAPItem = mapFMAP.get(pair.item);
						if(mapFMAPItem == null) {
							mapFMAPItem = new HashMap<Integer, Long>();
							mapFMAP.put(pair.item, mapFMAPItem);
						}
						for(int j = i+1; j< revisedTransaction.size(); j++){
							Pair pairAfter = revisedTransaction.get(j);
							Long twuSum = mapFMAPItem.get(pairAfter.item);
							if(twuSum == null) {
								mapFMAPItem.put(pairAfter.item, newTWU);
							}else {
								mapFMAPItem.put(pairAfter.item, twuSum + newTWU);
							}
						}
					}
					tid++;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
//		for (Integer item1:mapFMAP.keySet()) {
//			for (Integer item2:mapFMAP.get(item1).keySet()) {
//				long val=mapFMAP.get(item1).get(item2);
//				double EUB=(double)val/(stock.investMap.get(item1)+stock.investMap.get(item2)) ;
//				System.out.println(item1+" & "+item2+" : "+EUB);
//			}
//		}
		MemoryLogger.getInstance().checkMemory();
		HEPMIner(itemsetBuffer, 0, null, listOfUtilityLists, minEffiency);
		MemoryLogger.getInstance().checkMemory();
		writer.close();
		endTimestamp = System.currentTimeMillis();
	}

	private int compareItems2(int item1, int item2) {
		return  item1-item2;
	}

	/**
	 * This is the recursive method to find all high efficiency itemsets. It writes
	 * the itemsets to the output file.
	 * @param prefix  This is the current prefix. Initially, it is empty.
	 * @param pUL This is the efficiency List of the prefix. Initially, it is empty.
	 * @param ULs The efficiency lists corresponding to each extension of the prefix.
	 * @param minEffiency The minEffiency threshold.
	 * @param prefixLength The current prefix length
	 * @throws IOException
	 */
	private void HEPMIner(int [] prefix,
			int prefixLength, UtilityList pUL, List<UtilityList> ULs, double minEffiency)
			throws IOException {
		for(int i=0; i< ULs.size(); i++){
			candidateCount++;
			UtilityList X = ULs.get(i);
			double efficiency=(double)X.sumIutils/X.getInvest();
			if(Tool.compare(efficiency,minEffiency) >= 0){
				writeOut(prefix, prefixLength, X.item, X.sumIutils,X.getInvest(),efficiency);
			}
			double estimatEfficiency=(double)(X.sumIutils + X.sumRutils)/X.getInvest();
			if(Tool.compare(estimatEfficiency,minEffiency) >= 0){
				List<UtilityList> exULs = new ArrayList<UtilityList>();
				for(int j=i+1; j < ULs.size(); j++){
					UtilityList Y = ULs.get(j);
					Map<Integer, Long> mapTWUF = mapFMAP.get(X.item);
					if(mapTWUF != null) {
						Long twuF = mapTWUF.get(Y.item);
						int invest=X.getInvest()+Y.getInvest();
						if (pUL!=null){
							invest-= pUL.getInvest();
						}
						if(twuF == null || Tool.compare((double)twuF/invest,minEffiency) < 0) {
							continue;
						}
					}
					UtilityList temp = construct(pUL, X, Y, minEffiency);
					if(temp != null) {
						exULs.add(temp);
						joinCount++;
					}
				}
				itemsetBuffer[prefixLength] = X.item;
				HEPMIner(itemsetBuffer, prefixLength+1, X, exULs, minEffiency);
			}
		}
		MemoryLogger.getInstance().checkMemory();
	}
	
	/**
	 * This method constructs the utility list of pXY
	 * @param P :  the utility list of prefix P.
	 * @param px : the utility list of pX
	 * @param py : the utility list of pY
	 * @return the utility list of pXY
	 */
	private UtilityList construct(UtilityList P, UtilityList px, UtilityList py, double minEffiency) {
		Integer invest=px.getInvest()+py.getInvest();
		if (P!=null){
			invest-=P.getInvest();
		}
		UtilityList pxyUL = new UtilityList(py.item,invest);
		long totalUtility = px.sumIutils + px.sumRutils;
		for(Element ex : px.elements){
			Element ey = findElementWithTID(py, ex.tid);
			if(ey == null){
				if(ENABLE_LA_PRUNE) {
					totalUtility -= (ex.iutils+ex.rutils);
					if(Tool.compare((double)totalUtility/px.getInvest(),minEffiency) < 0) {
						return null;
					}
				}
				continue;
			}
			if(P == null){
				Element eXY = new Element(ex.tid, ex.iutils + ey.iutils, ey.rutils);
				pxyUL.addElement(eXY);
				
			}else{
				Element e = findElementWithTID(P, ex.tid);
				if(e != null){
					Element eXY = new Element(ex.tid, ex.iutils + ey.iutils - e.iutils,
								ey.rutils);
					pxyUL.addElement(eXY);
				}
			}	
		}
		return pxyUL;
	}
	
	/**
	 * Do a binary search to find the element with a given tid in a utility list
	 * @param ulist the utility list
	 * @param tid  the tid
	 * @return  the element or null if none has the tid.
	 */
	private Element findElementWithTID(UtilityList ulist, int tid){
		List<Element> list = ulist.elements;
		
		// perform a binary search to check if  the subset appears in  level k-1.
        int first = 0;
        int last = list.size() - 1;
       
        // the binary search
        while( first <= last )
        {
        	int middle = ( first + last ) >>> 1; // divide by 2

            if(list.get(middle).tid < tid){
            	first = middle + 1;  //  the itemset compared is larger than the subset according to the lexical order
            }
            else if(list.get(middle).tid > tid){
            	last = middle - 1; //  the itemset compared is smaller than the subset  is smaller according to the lexical order
            }
            else{
            	return list.get(middle);
            }
        }
		return null;
	}

	/**
	 * Method to write a high utility itemset to the output file.
	 * @param  prefix to be writent o the output file
	 * @param  item to be appended to the prefix
	 * @param utility the utility of the prefix concatenated with the item
	 * @param prefixLength the prefix length
	 */
	private void writeOut(int[] prefix, int prefixLength, int item, long utility, Integer invest,double efficiency) throws IOException {
		heiCount++;
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < prefixLength; i++) {
			buffer.append(prefix[i]);
			buffer.append(' ');
		}
		buffer.append(item);
		buffer.append(" #UTIL: ");
		buffer.append(utility);
		buffer.append(" #INVEST: ");
		buffer.append(invest);
		buffer.append(" #EFFICIENCY: ");
		buffer.append(efficiency);
		writer.write(buffer.toString());
		writer.newLine();
	}

	
	/**
	 * Print statistics about the latest execution to System.out.
	 * @throws IOException 
	 */
	public void printStats() throws IOException {
		System.out.println("=============  HEPMiner ALGORITHM - STATS =============");
		System.out.println(" Total time ~ "                  + (double)(endTimestamp - startTimestamp)/1000 + " s");
		System.out.println(" Memory ~ "                      + MemoryLogger.getInstance().getMaxMemory()  + " MB");
		System.out.println(" Candidate count : "             + candidateCount);
		System.out.println(" Joint count : "             + joinCount);
		System.out.println(" High-efficiency itemsets count : " + heiCount);
		if(DEBUG) {
			int pairCount = 0;
			double maxMemory = getObjectSize(mapFMAP);
			for(Entry<Integer, Map<Integer, Long>> entry : mapFMAP.entrySet()) {
				maxMemory += getObjectSize(entry.getKey());
				for(Entry<Integer, Long> entry2 :entry.getValue().entrySet()) {
					pairCount++;
					maxMemory += getObjectSize(entry2.getKey()) + getObjectSize(entry2.getValue());
				}
			}
			System.out.println("CMAP size " + maxMemory + " MB");
			System.out.println("PAIR COUNT " + pairCount);
		}
		System.out.println("===================================================");
	}
	
	/**
	 * Get the size of a Java object (for debugging purposes)
	 * @param object the object
	 * @return the size in MB
	 * @throws IOException
	 */
    private double getObjectSize(
            Object object)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.close();
        double maxMemory = baos.size() / 1024d / 1024d;
        return maxMemory;
    }
}