package it.unibo.csr.big.cubeload.schema;
/**
 * This class implements a fundamental component of an OLAP query:
 * the selection predicate. A selection predicate is based on a
 * hierarchy level and constrains it to a single value; it may be
 * a segregation predicate, an year prompt or a normal predicate.
 * @author Luca Spadazzi
 *
 */
public class SelectionPredicate
{
	/**
	 * Class fields
	 */
	private String hierarchy;
	private String level;
	private String element;
	private boolean yearPrompt, segregationPredicate;
	
	/**
	 * Class constructor.
	 * @param hierarchy The selection predicate's signature hierarchy.
	 * @param level The level of the hierarchy the predicate will constrain.
	 * @param element An element taken from the level's distinct values.
	 * @param yearPrompt A flag, set if the predicate is an year prompt.
	 * @param segregationPredicate A flag, set if the predicate is a segregation predicate.
	 */
	public SelectionPredicate(String hierarchy,
							  String level,
							  String element,
							  boolean yearPrompt,
							  boolean segregationPredicate)
	{
		this.hierarchy = hierarchy;
		this.level = level;
		this.element = element;
		this.yearPrompt = yearPrompt;
		this.segregationPredicate = segregationPredicate;
	}
	
	/**
	 * This method builds a string using the selection predicate's literal components.
	 * @return The concatenation of the predicate's hierarchy, level and element.
	 */
	public String getSelectionPredicate()
	{
		String selPred = hierarchy.toUpperCase()
					   + "."
				       + level.toUpperCase()
				       + " = "
				       + element.toUpperCase();
		
		return selPred;
	}
	
	/**
	 * Getter method for the selection predicate's hierarchy component.
	 * @return The name of the predicate's hierarchy.
	 */
	public String getHierarchy()
	{
		return this.hierarchy;
	}
	
	/**
	 * Getter method for the selection predicate's level component.
	 * @return The name of the predicate's level.
	 */
	public String getLevel()
	{
		return this.level;
	}
	
	/**
	 * Getter method for the selection predicate's element component.
	 * @return The name of the predicate's element.
	 */
	public String getElement()
	{
		return this.element;
	}
	
	/**
	 * Getter method for the selection predicate's year prompt flag.
	 * @return True if the predicate is an year prompt, false otherwise.
	 */
	public boolean getPrompt()
	{
		return this.yearPrompt;
	}
	
	/**
	 * Getter method for the selection predicate's segregation predicate flag.
	 * @return True if the predicate is asegregation predicate, false otherwise.
	 */
	public boolean getSegregation()
	{
		return this.segregationPredicate;
	}

	/**
	 * This method checks if the current selection predicate matches completely with a given one.
	 * @param sel The selection predicate we confront with the current one.
	 * @return True if every field of the current selection predicate matches with the
	 * corresponding field of the given predicate, False otherwise.
	 */
	public boolean matchesWith(SelectionPredicate sel)
	{
		if (this.getHierarchy().equals(sel.getHierarchy()) &&
			this.getLevel().equals(sel.getLevel()) &&
			this.getElement().equals(sel.getElement()) &&
			this.getPrompt() == sel.getPrompt() &&
			this.getSegregation() == sel.getSegregation())
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((element == null) ? 0 : element.hashCode());
		result = prime * result
				+ ((hierarchy == null) ? 0 : hierarchy.hashCode());
		result = prime * result + ((level == null) ? 0 : level.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SelectionPredicate)) {
			return false;
		}
		
		SelectionPredicate other = (SelectionPredicate) obj;
		
		if (element == null) {
			if (other.element != null) {
				return false;
			}
		} else if (!element.equals(other.element)) {
			return false;
		}
		
		if (hierarchy == null) {
			if (other.hierarchy != null) {
				return false;
			}
		} else if (!hierarchy.equals(other.hierarchy)) {
			return false;
		}
		
		if (level == null) {
			if (other.level != null) {
				return false;
			}
		} else if (!level.equals(other.level)) {
			return false;
		}
		
		return true;
	}
}
