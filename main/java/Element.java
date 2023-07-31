public class Element {
	// The three variables as described in the paper:
	/** transaction id */
	public final int tid ;   
	/** itemset utility */
	public int iutils;
	/** remaining utility */
	public int rutils; 
	
	/**
	 * Constructor.
	 * @param tid  the transaction id
	 * @param iutils  the itemset utility
	 * @param rutils  the remaining utility
	 */
	public Element(int tid, int iutils, int rutils){
		this.tid = tid;
		this.iutils = iutils;
		this.rutils = rutils;
	}

	public Element(int tid) {
		this.tid = tid;
	}
}
