package it.unibo.csr.big.cubeload.schema;
public class GroupByElement
{
	private String hierarchy;
	private String level;
	private boolean visible;
	
	/**
	 * Class constructor
	 */
	public GroupByElement(String hierarchy, String level)
	{
		this.hierarchy = hierarchy;
		this.level = level;
		visible = true;
	}
	
	/**
	 * Getter method for the group-by element's hierarchy.
	 * @return The group-by's hierarchy.
	 */
	public String getHierarchy()
	{
		return hierarchy;
	}
	
	/**
	 * Getter method for the group-by element's level.
	 * @return The group-by's level.
	 */
	public String getLevel()
	{
		return level;
	}
	
	/**
	 * Setter method for the group-by element's level.
	 * @param level The level we want to set for this element.
	 */
	public void setLevel(String level)
	{
		this.level = level;
	}
	
	/**
	 * Getter method for the group-by element's visibility.
	 * @return The group-by's visibility.
	 */
	public boolean getVisible()
	{
		return visible;
	}
	
	/**
	 * Setter method for the group-by element's visibility.
	 * @param value The boolean value to be set.
	 */
	public void setVisible(boolean value)
	{
		this.visible = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((hierarchy == null) ? 0 : hierarchy.hashCode());
		result = prime * result + ((level == null) ? 0 : level.hashCode());
		result = prime * result + (visible ? 1231 : 1237);
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
		if (!(obj instanceof GroupByElement)) {
			return false;
		}
		
		GroupByElement other = (GroupByElement) obj;
		
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
		
		if (visible != other.visible) {
			return false;
		}
		
		return true;
	}
}