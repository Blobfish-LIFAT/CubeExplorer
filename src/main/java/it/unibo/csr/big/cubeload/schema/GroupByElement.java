package it.unibo.csr.big.cubeload.schema;

import lombok.Getter;

import java.util.Objects;

public class GroupByElement
{
	@Getter
	Hierarchy h;
	private String level;
	@Getter
	Level l;
	private boolean visible;


	public GroupByElement(Hierarchy hierarchy, Level level){
		h = hierarchy;
		l = level;
		this.level = level.getName();
	}
	
	/**
	 * Getter method for the group-by element's hierarchy.
	 * @return The group-by's hierarchy.
	 */
	public String getHierarchy()
	{
		return h.getName();
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
				+ ((h == null) ? 0 : h.getName().hashCode());
		result = prime * result + ((level == null) ? 0 : level.hashCode());
		result = prime * result + (visible ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GroupByElement that = (GroupByElement) o;
		return visible == that.visible &&
				Objects.equals(h, that.h) &&
				Objects.equals(level, that.level);
	}
}