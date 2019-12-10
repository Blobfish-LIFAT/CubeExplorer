package it.unibo.csr.big.cubeload.schema;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class implements a cube's hierarchy and the methods to
 * manipulate them (hierarchy levels). 
 * @author Luca Spadazzi
 * 
 */
public class Hierarchy
{
	private static final int SECOND_LAST_PADDING = 2;
	private String name;
	@Getter @Setter
	private mondrian.olap.Hierarchy md;
	private List<Level> levels = new ArrayList<Level>();
	Random rand = new Random();
	
	/**
	 * Class constructor.
	 * @param name The name of the hierarchy.
	 */
	public Hierarchy (String name)
	{
		this.name = name;
	}
	
	/**
	 * Getter method for the hierarchy name.
	 * @return The name of the hierarchy.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Getter method for the hierarchy levels.
	 * @return The list of levels.
	 */
	public List<Level> getLevels ()
	{
		return levels;
	}
	
	/**
	 * Getter method for a particular level.
	 * @param number The index of the level we need.
	 * @return The level contained at the given index.
	 */
	public Level getLevel (int number)
	{
		return levels.get(number);
	}
	
	/**
	 * Getter method for the level with a given name.
	 * @param levelName The name of the level we're looking for.
	 * @return The level with the given name.
	 */
	public Level getLevel(String levelName)
	{
		for (Level lev : levels)
		{
			if (lev.getName().equals(levelName))
			{
				return lev;
			}
		}
		
		return null;
	}
	
	/**
	 * Getter method for the number of levels.
	 * @return The size of the level list.
	 */
	public int getLevelCount()
	{
		return levels.size();
	}
	
	/**
	 * Getter method for a random level: the range includes the ALL level according to the flag.
	 * @param allIncluded The flag that determines the range.
	 * @return A randomly chosen level in the correct range.
	 */
	public Level getRandomLevel(boolean allIncluded)
	{
		int range = allIncluded ? getLevelCount() : getLevelCount() - 1;
		
		return levels.get(rand.nextInt(range));
	}
	
	/**
	 * This method adds a level in the hierarchy level list. Since in the input files levels
	 * are usually indexed from most to least aggregated, the levels are added on top of the list.
	 * @param level The level to be added to the list.
	 */
	public void addLevel(Level level)
	{
		levels.add(0, level);
	}
	
	/**
	 * This method checks the position of a level in the current hierarchy.
	 * @param levelName The name of the level to find.
	 * @return The position of the level if it's present, -1 otherwise.
	 */
	public int findPosition(String levelName)
	{
		int position = -1;
		String currentLevel;
		
		for (int i = 0; i < levels.size(); ++i)
		{
			currentLevel = levels.get(i).getName();
			
			if (currentLevel.equals(levelName))
			{
				position = i;
				break;
			}
		}
		
		return position;
	}

	/**
	 * This method checks if, in the current hierarchy, a level is ancestor of another level.
	 * @param level1 The level we're checking for being an ancestor.
	 * @param level2 The level we're checking for begin a descendant.
	 * @return True if the first level is an ancestor of the second level, False otherwise.
	 */
	public boolean isAncestor (String level1, String level2)
	{
		int pos1 = findPosition(level1);
		int pos2 = findPosition(level2);
		
		if (pos1 != -1 && pos2 != -1 && pos1 <= pos2)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * This method checks if a particular level is the
	 * most aggregated level in the current hierarchy.
	 * @param level The level we want to know if it's the leaf.
	 * @return True if this level is the leaf, False otherwise.
	 */
	public boolean isMaxLevel (String level)
	{
		return (findPosition(level) == getLevelCount() - 1 ? true : false);
	}

	/**
	 * This method populates all "non-ALL" levels of the current hierarchy.
	 * @param valueList The list of values we populate the levels with.
	 */
	public void setLevelValues(List<String> valueList)
	{
		for (int i = 0; i < levels.size() - 1; ++i)
		{
			levels.get(i).addDistinctValues(valueList.get(i));
		}
	}
	
	/**
	 * This method returns a valid position for a selection predicate
	 * on a hierarchy, based on the position of the query in that hierarchy.
	 * @param levelPosition The position of the query on this hierarchy.
	 * @return An index greater than the position of the query and lesser
	 * than the maximum level.
	 */
	public int getValidPredicatePosition(int levelPosition)
	{
		Random rand = new Random();
		
		return 1 + levelPosition + rand.nextInt(getLevelCount() - levelPosition - SECOND_LAST_PADDING);
	}

	@Override
	public String toString() {
		return "Hierarchy {" +
				"name='" + name + '\'' +
				", levels=" + levels +
				'}';
	}
}
