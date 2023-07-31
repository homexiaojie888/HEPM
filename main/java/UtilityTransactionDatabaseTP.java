import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UtilityTransactionDatabaseTP {

	private final Set<Integer> allItems = new HashSet<Integer>();
	private final List<TransactionTP> transactions = new ArrayList<TransactionTP>();


	/**
	 * Load a transaction database from a file.
	 * @param path the path of the file
	 * @throws IOException exception if error while reading the file.
	 */
	public void loadFile(String path) throws IOException {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			// for each transaction (line) in the input file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}
				
				// process the transaction
				processTransaction(thisLine.split(":"));
			}
		} catch (Exception e) {
			// catch exceptions
			e.printStackTrace();
		}finally {
			if(myInput != null){
				// close the file
				myInput.close();
			}
	    }
	}
	
	/**
	 * Process a line (transaction) from the input file
	 * @param line  a line
	 */
	private void processTransaction(String[] line){
		// get the transaction utility
		int transactionUtility = Integer.parseInt(line[1]);
		
		String[] items = line[0].split(" ");
		String[] utilities = line[2].split(" ");
		
		// Create a list for storing items
		List<ItemUtility> itemUtilityObjects = new ArrayList<ItemUtility>();
		// for each item
		for(int i=0; i< items.length; i++) {
			itemUtilityObjects.add(new ItemUtility(
					Integer.parseInt(items[i]),
					Integer.parseInt(utilities[i])));
		}

		// add the transaction to the list of transactions
		transactions.add(new TransactionTP(itemUtilityObjects, transactionUtility));
	}

	/**
	 * Print this database to System.out.
	 */
	public void printDatabase(){
		System.out
		.println("===================  Database ===================");
		int count = 0;
		// for each transaction
		for(TransactionTP itemset : transactions){
			// print the transaction
			System.out.print("0" + count + ":  ");
			itemset.print();
			System.out.println("");
			count++;
		}
	}
	
	/**
	 * Get the number of transactions.
	 * @return a int
	 */
	public int size(){
		return transactions.size();
	}

	/**
	 * Get the list of transactions.
	 * @return the list of Transactions.
	 */
	public List<TransactionTP> getTransactions() {
		return transactions;
	}

	/**
	 * Get the set of items in this database.
	 * @return a Set of Integers
	 */
	public Set<Integer> getAllItems() {
		return allItems;
	}

}
