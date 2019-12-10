package it.unibo.csr.big.cubeload.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class defines the features of a cube's dimension
 * (hierarchies), and methods to set and manipulate them.
 * @author Luca Spadazzi
 * 
 */
public class Dimension
{
	private String name;
	private List<Hierarchy> hierarchies = new ArrayList<Hierarchy>();
	private boolean temporal;
	Random rand = new Random();
	
	/**
	 * Class constructor.
	 * @param name The name of the dimension.
	 */
	public Dimension(String name)
	{
		this.name = name;
	}
	
	/**
	 * Getter method for the dimension's name.
	 * @return The name of the dimension.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Getter method for the dimension's hierarchies.
	 * @return The list of hierarchies of the dimension.
	 */
	public List<Hierarchy> getHierarchies()
	{
		return hierarchies;
	}
	
	/**
	 * Getter method for a particular hierarchy.
	 * @param number The index of the hierarchy we need.
	 * @return The hierarchy contained in the given index.
	 */
	public Hierarchy getHierarchy(int number)
	{
		return hierarchies.get(number);
	}
	
	/**
	 * Getter method for a random hierarchy.
	 * @return The hierarchy randomly chosen.
	 */
	public Hierarchy getRandomHierarchy()
	{
		return hierarchies.get(rand.nextInt(getHierarchyCount()));
	}
	
	/**
	 * Getter method for the number of hierarchies.
	 * @return The size of the dimension's hierarchy list.
	 */
	public int getHierarchyCount()
	{
		return hierarchies.size();
	}
	
	/**
	 * This method adds a hierarchy to the dimension.
	 * @param hierarchy The hierarchy to be added to the list.
	 */
	public void addHierarchy (Hierarchy hierarchy)
	{
		hierarchies.add(hierarchy);
	}
	
	/**
	 * Getter method for the temporal flag.
	 * @return True if the dimension is temporal, False otherwise.
	 */
	public boolean isTemporal()
	{
		return temporal;
	}
	
	/**
	 * Setter method for the temporal flag.
	 * @param value The value to set the temporal flag.
	 */
	public void setTemporal(boolean value)
	{
		temporal = value;
	}
}
