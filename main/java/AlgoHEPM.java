import java.util.*;


/**
 * This is an implementation of the "HEPM" for High-Efficiency pattern Mining.
 * <br/><br/>
 * @see ItemsetsTP
 * @see ItemsetTP
 * @see TransactionTP
 * @author xiaojie Zhang
 */
public class AlgoHEPM {
	private ItemsetsTP highEfficiencyItemsets = null;
	protected UtilityTransactionDatabaseTP database;
	protected Stock stock;
	double minEficiency;
	long startTimestamp = 0;
	long endTimestamp = 0;
	private int candidatesCount;
	private int candidatesHEUBCount;
	public AlgoHEPM() {
	}

	/**
	 * Run the HEPM algorithm
	 * @param database  a transaction database containing utility information.
	 * @param minEficiency the min Effiency threshold
	 * @return the set of high efficiency itemsets
	 */
	public ItemsetsTP runAlgorithm(UtilityTransactionDatabaseTP database,Stock stock, double minEficiency) {
		this.database = database;
		this.stock=stock;
		this.minEficiency=minEficiency;
		MemoryLogger.getInstance().reset();
		startTimestamp = System.currentTimeMillis();
		highEfficiencyItemsets = new ItemsetsTP("HIGH Efficiency ITEMSETS");
		candidatesCount =0;
		candidatesHEUBCount=0;
		// ===================  PHASE 1: GENERATE CANDIDATES  ===================
		List<ItemsetTP> candidatesSize1 = new ArrayList<ItemsetTP>();
		Map<Integer, Set<Integer>> mapItemTidsets = new HashMap<Integer, Set<Integer>>();
		Map<Integer, Integer> mapItemTWU = new HashMap<Integer, Integer>();
		for(int i=0; i< database.size(); i++){
			TransactionTP transaction = database.getTransactions().get(i);
			for(int j=0; j< transaction.getItems().size(); j++) {
				ItemUtility itemUtilityObj = transaction.getItems().get(j);
				int item = itemUtilityObj.item;
				Set<Integer> tidset = mapItemTidsets.get(item);
				if(tidset == null){
					tidset = new HashSet<Integer>();
					mapItemTidsets.put(item, tidset);
				}
				tidset.add(i);

				Integer sumUtility = mapItemTWU.get(item);
				if(sumUtility == null){
					sumUtility = 0;
				}
				sumUtility += transaction.getTransactionUtility();
				mapItemTWU.put(item, sumUtility);
			}
		}

		for (Integer item:mapItemTWU.keySet()) {
			Integer TWU = mapItemTWU.get(item);
			Integer invest=stock.investMap.get(item);
			double estimatedEfficiency=(double)TWU/invest;
			if(Tool.compare(estimatedEfficiency,minEficiency) >= 0){
				ItemsetTP itemset = new ItemsetTP();
				itemset.addItem(item);
				itemset.setTIDset(mapItemTidsets.get(item));
				itemset.setInvest(invest);
				candidatesSize1.add(itemset);
				highEfficiencyItemsets.addItemset(itemset, itemset.size());

			}
		}
		List<ItemsetTP> currentLevel = candidatesSize1;
		candidatesCount=currentLevel.size();
		Collections.sort(currentLevel, new Comparator<ItemsetTP>() {
			@Override
			public int compare(ItemsetTP o1, ItemsetTP o2) {
				List<Integer> l1=o1.getItems();
				List<Integer> l2=o2.getItems();
				return l1.get(l1.size()-1)-l2.get(l2.size()-1);
			}
		});
		while (true) {
			int tmpcandidateHTWUCount = highEfficiencyItemsets.getItemsetsCount();
			currentLevel = generateCandidateSizeK(currentLevel, highEfficiencyItemsets);
			if(tmpcandidateHTWUCount == highEfficiencyItemsets.getItemsetsCount()){
				break;
			}
		}
		MemoryLogger.getInstance().checkMemory();
		candidatesHEUBCount = highEfficiencyItemsets.getItemsetsCount();

		// ========================  PHASE 2: Calculate exact efficiency of each candidate =============
		for(List<ItemsetTP> level : highEfficiencyItemsets.getLevels()){
			Iterator<ItemsetTP> iterItemset = level.iterator();
			while(iterItemset.hasNext()){
				ItemsetTP candidate = iterItemset.next();
				Integer invest=candidate.getInvest();
				for (int tid = 0; tid < database.getTransactions().size(); tid++) {
					TransactionTP transaction=database.getTransactions().get(tid);
					int transactionUtility =0;
					int matchesCount =0;
					for(int i=0; i< transaction.size(); i++){
						if(candidate.getItems().contains(transaction.get(i).item)){
							transactionUtility += transaction.getItemsUtilities().get(i).utility;
							matchesCount++;
						}
					}
					if(matchesCount == candidate.size()){
						candidate.incrementUtility(transactionUtility);
					}
				}
				double efficiency=(double)candidate.getUtility()/invest;
				candidate.setEfficiency(efficiency);

				if(Tool.compare(efficiency,minEficiency)<0){
					iterItemset.remove();
					highEfficiencyItemsets.decreaseCount();
				}
				
			}
		}

		MemoryLogger.getInstance().checkMemory();

		endTimestamp = System.currentTimeMillis();

		return highEfficiencyItemsets;
	}

	protected List<ItemsetTP> generateCandidateSizeK(List<ItemsetTP> levelK_1, ItemsetsTP candidatesHTWUI) {

	loop1:	for(int i=0; i< levelK_1.size(); i++){
				ItemsetTP itemset1 = levelK_1.get(i);

	loop2:		for(int j=i+1; j< levelK_1.size(); j++){
					ItemsetTP itemset2 = levelK_1.get(j);
					int invest=0;
				// we compare items of itemset1  and itemset2.
				// If they have all the same k-1 items and the last item of itemset1 is smaller than
				// the last item of itemset2, we will combine them to generate a candidate
					for(int k=0; k< itemset1.size(); k++){
						invest+=stock.investMap.get(itemset1.getItems().get(k));
						if(k == itemset1.size()-1){
							if(itemset1.getItems().get(k) >= itemset2.get(k)){
								System.out.println("loop1");
								continue loop1;

							}
						}
						else if(itemset1.getItems().get(k) < itemset2.get(k)){
							continue loop2;
						}
						else if(itemset1.getItems().get(k) > itemset2.get(k)){
							System.out.println("loop1");
							continue loop1;
						}
					}
				candidatesCount++;
				Integer missing = itemset2.get(itemset2.size()-1);
				invest+=stock.investMap.get(missing);
				Set<Integer> tidset = new HashSet<Integer>();
				for(Integer val1 : itemset1.getTIDset()){
					if(itemset2.getTIDset().contains(val1)){
						tidset.add(val1);
					}
				}

				int twu =0;
				for(Integer tid : tidset){
					twu += database.getTransactions().get(tid).getTransactionUtility();
				}
		 		double estaminteEfficiency=(double)twu/invest;
				if(Tool.compare(estaminteEfficiency,minEficiency) >= 0){
					ItemsetTP candidate = new ItemsetTP();
					for(int k=0; k < itemset1.size(); k++){
						candidate.addItem(itemset1.get(k));
					}
					candidate.addItem(missing);
					candidate.setTIDset(tidset);
					candidate.setInvest(invest);
					candidatesHTWUI.addItemset(candidate, candidate.size());
				}
			}
		}
		return candidatesHTWUI.getLevels().get(candidatesHTWUI.getLevels().size()-1);
	}

	public void printStats() {
		System.out
				.println("=============  HEPM ALGORITHM - STATS =============");
		System.out.println(" Transactions count from database : "+ database.size());
		System.out.println(" Total time ~ " + (double)(endTimestamp - startTimestamp)/1000 + " s");
		System.out.println(" Memeory usage: "
				+ MemoryLogger.getInstance().getMaxMemory()+ " MB");
		System.out.println(" Candidates count : " + candidatesCount);
		System.out.println(" Candidates high EUB count : " + candidatesHEUBCount);
		System.out.println(" High-efficiency itemsets count : " + highEfficiencyItemsets.getItemsetsCount());
		System.out.println("===================================================");
	}

}