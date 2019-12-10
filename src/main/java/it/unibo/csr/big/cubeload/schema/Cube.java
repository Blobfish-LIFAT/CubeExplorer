package it.unibo.csr.big.cubeload.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class implements an OLAP cube, namely a set of
 * dimensions, hierarchies and measures. The methods
 * manipulate or perform checks on the cube's fields.
 * @author Luca Spadazzi
 * 
 */
public class Cube
{
	private String name;
	private List<Dimension> dimensions= new ArrayList<Dimension>();
	private List<Measure> measures = new ArrayList<Measure>();
	Random rand = new Random();
	
	/**
	 * Class constructor
	 * @param name The name we set for the cube.
	 */
	public Cube (String name)
	{
		this.name = name;
	}
	
	/**
	 * Getter method for the cube name.
	 * @return The name of the cube.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * This method adds a dimension to the dimension list.
	 * @param dimension The dimension to be added.
	 */
	public void addDimension (Dimension dimension)
	{
		dimensions.add(dimension);
	}
	
	/**
	 * Getter method for the number of the cube's dimensions.
	 * @return The dimension list's size.
	 */
	public int getDimensionCount()
	{
		return dimensions.size();
	}
	
	/**
	 * Getter method for the dimensions.
	 * @return The cube's dimension list.
	 */
	public List<Dimension> getDimensions()
	{
		return dimensions;
	}
	
	/**
	 * Getter method for a particular dimension.
	 * @param number The index of the dimension we want.
	 * @return The dimension contained at the given index.
	 */
	public Dimension getDimension(int number)
	{
		return dimensions.get(number);
	}
	
	/**
	 * Getter method for the temporal dimension.
	 * @return The cube's temporal dimension.
	 */
	public Dimension getTemporalDimension()
	{
		for (Dimension dim : dimensions)
		{
			if (dim.isTemporal())
			{
				return dim;
			}
		}
		
		return null;
	}
	
	/**
	 * Getter method for a random dimension.
	 * @return A dimension of the cube, randomly chosen.
	 */
	public Dimension getRandomDimension()
	{
		return dimensions.get(rand.nextInt(getDimensionCount()));
	}
	
	/**
	 * Getter method for the number of measures.
	 * @return The size of the cube's measure list.
	 */
	public int getMeasureCount()
	{
		return measures.size();
	}
	
	/**
	 * This method adds a measure to the cube's measure list.
	 * @param measure The measure to be added.
	 */
	public void addMeasure (Measure measure)
	{
		measures.add(measure);
	}
	
	/**
	 * Getter method for the number of measures.
	 * @return The size of the cube's measure list.
	 */
	public List<Measure> getMeasures()
	{
		return measures;
	}
	
	/**
	 * Getter method for a particular measure.
	 * @param number The index of the measure we want.
	 * @return The measure contained at the given index.
	 */
	public Measure getMeasure(int number)
	{
		return measures.get(number);
	}
	
	/**
	 * This method chooses a fixed amount of distinct measures.
	 * @param number The number of measures we need.
	 * @return The list of distinct measures produced.
	 */
	public List<Measure> getRandomMeasures(int number)
	{
		List<Measure> tempList = new ArrayList<Measure>();
		Measure tempMeas;
		
		// Exactly 'number' Measures must be added
		while (tempList.size() < number)
		{
			tempMeas = getRandomMeasure(); // A random Measure is selected
			boolean present = false;
			
			for (Measure meas : tempList)
			{
				if (meas.getName().equals(tempMeas.getName()))
					present = true;
			}
			
			if (! present)
				tempList.add(tempMeas); // If not present, the Measure is added
		}
		
		return tempList;
	}
	
	/**
	 * Getter method for a random measure of the cube.
	 * @return The randomly chosen measure.
	 */
	private Measure getRandomMeasure()
	{
		return measures.get(rand.nextInt(getMeasureCount()));
	}
	
	/**
	 * This method looks for a hierarchy in the cube with the given name.
	 * @param hierarchy The name of the hierarchy we need.
	 * @return The cube's hierarchy with the given name.
	 */
	public Hierarchy findHierarchy(String hierarchy)
	{
		for (Hierarchy hie : getHierarchies())
		{
			if (hie.getName().equals(hierarchy))
			{
				return hie;
			}
		}
		
		return null;
	}
	
	/**
	 * Getter method for the number of the cube's hierarchies.
	 * @return The total amount of hierarchies in the cube.
	 */
	public int getTotalHierarchyCount()
	{		
		return getHierarchies().size();
	}
	
	/**
	 * Getter method for the cube's hierarchies.
	 * @return The list of total hierarchies in the cube. 
	 */
	public List<Hierarchy> getHierarchies()
	{
		List<Hierarchy> hierarchies = new ArrayList<Hierarchy>();
		
		for (Dimension dim : dimensions)
		{
			hierarchies.addAll(dim.getHierarchies());
		}
		
		return hierarchies;
	}
	
	/**
	 * Getter method for a random hierarchy in the cube.
	 * @return The hierarchy randomly chosen.
	 */
	public Hierarchy getRandomHierarchy()
	{
		return this.getHierarchies().get(rand.nextInt(getTotalHierarchyCount()));
	}
	
	/**
	 * This method checks if the given hierarchy belongs to the temporal dimension.
	 * @param hierarchy The hierarchy we're checking for being temporal.
	 * @return True if the given hierarchy is temporal, False otherwise.
	 */
	public boolean isTemporalHierarchy(Hierarchy hierarchy)
	{
		for (Dimension dim : dimensions)
		{
			if (dim.isTemporal() && dim.getHierarchies().contains(hierarchy))
			{
				return true;
			}
		}
		
		return false;
	}
}
