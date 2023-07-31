import java.util.ArrayList;
import java.util.List;

public class UtilityList {
	 Integer item;  // the item
	 Integer invest=0;
	 long sumIutils = 0;  // the sum of item utilities
	 long sumRutils = 0;  // the sum of remaining utilities
	 List<Element> elements = new ArrayList<Element>();  // the elements

	public Integer getInvest() {
		return invest;
	}

	public void setInvest(Integer invest) {
		this.invest = invest;
	}

	/**
	 * Constructor.
	 * @param item the item that is used for this utility list
	 */
	public UtilityList(Integer item){
		this.item = item;
	}

	public UtilityList(Integer item, Integer invest) {
		this.item = item;
		this.invest = invest;
	}

	/**
	 * Method to add an element to this utility list and update the sums at the same time.
	 */
	public void addElement(Element element){
		sumIutils += element.iutils;
		sumRutils += element.rutils;
		elements.add(element);
	}
	
	/**
	 * Get the support of the itemset represented by this utility-list
	 * @return the support as a number of trnsactions
	 */
	public int getSupport() {
		return elements.size();
	}
}
