
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ItemsetTP{
	/** an itemset is an ordered list of items */
	private final List<Integer> items = new ArrayList<Integer>(); 
	/** we also indicate the utility of the itemset */
	private int utility =0;
	/** this is the set of tids (ids of transactions) containing this itemset */
	private Set<Integer> transactionsIds = null;
	private int invest =0;
	private double efficiency=0.0;

	public double getEfficiency() {
		return efficiency;
	}

	public void setEfficiency(double efficiency) {
		this.efficiency = efficiency;
	}

	/**
	 * Default constructor
	 */
	public ItemsetTP(){
	}

	public int getInvest() {
		return invest;
	}

	public void setInvest(int invest) {
		this.invest = invest;
	}

	/**
	 * Get the relative support of this itemset
	 * @param nbObject  the number of transactions
	 * @return the support
	 */
	public double getRelativeSupport(int nbObject) {
		return ((double)transactionsIds.size()) / ((double) nbObject);
	}
	
	/**
	 * Get the relative support of this itemset
	 * @param nbObject  the number of transactions
	 * @return the support
	 */
	public String getRelativeSupportAsString(int nbObject) {
		// calculate the support
		double frequence = ((double)transactionsIds.size()) / ((double) nbObject);
		// format it to use two decimals
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(4); 
		// return the formated support
		return format.format(frequence);
	}
	
	/**
	 * Get the absolute support of that itemset
	 * @return the absolute support (integer)
	 */
	public int getAbsoluteSupport(){
		return transactionsIds.size();
	}

	/**
	 * Add an item to that itemset
	 * @param value the item to be added
	 */
	public void addItem(Integer value){
			items.add(value);
	}
	
	/**
	 * Get items from that itemset.
	 * @return a list of integers (items).
	 */
	public List<Integer> getItems(){
		return items;
	}
	
	/**
	 * Get the item at at a given position in that itemset
	 * @param index the position
	 * @return the item (Integer)
	 */
	public Integer get(int index){
		return items.get(index);
	}
	
	/**
	 * print this itemset to System.out.
	 */
	public void print(){
		System.out.print(toString());
	}
	
	/**
	 * Get a string representation of this itemset
	 * @return a string
	 */
	public String toString(){
		// create a string buffer
		StringBuilder r = new StringBuilder ();
		// for each item
		for(Integer attribute : items){
			// append it
			r.append(attribute.toString());
			r.append(' ');
		}
		// return the string
		return r.toString();
	}

	/**
	 * Set the tidset of this itemset.
	 * @param listTransactionIds  a set of tids as a Set<Integer>
	 */
	public void setTIDset(Set<Integer> listTransactionIds) {
		this.transactionsIds = listTransactionIds;
	}
	
	/**
	 * Get the number of items in this itemset
	 * @return the item count (int)
	 */
	public int size(){
		return items.size();
	}

	/**
	 * Get the set of transactions ids containing this itemset
	 * @return  a tidset as a Set<Integer>
	 */
	public Set<Integer> getTIDset() {
		return transactionsIds;
	}

	/**
	 * Get the utility of this itemset.
	 * @return utility as an int
	 */
	public int getUtility() {
		return utility;
	}
	
	/**
	 * Increase the utility of this itemset by a given amount.
	 * @param increment  the amount.
	 */
	public void incrementUtility(int increment){
		utility += increment;
	}
}
