package it.unibo.csr.big.cubeload.generator;

import it.unibo.csr.big.cubeload.schema.*;
import it.unibo.csr.big.cubeload.template.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * This class implements the concept of profile. The profile's features
 * describe its characterization: seed queries, session length range
 * etc. In this class the full list of sessions is formed and returned. 
 * @author Luca Spadazzi
 * 
 * NOTE:
 * By default, the algorithm assigns each template a 25% chance of
 * being picked. In order to alter this behavior, enable to constant
 * ENABLE_CUSTOM_QUOTAS by setting it to true and assign each template
 * a custom percentage (between 0 and 100). Remember that the sum
 * of the constants should be 100. 
 * @author Enrico Gallinucci
 * 
 */
public class Profile
{
	/**
	 * Class fields
	 */
	private List<Query> seedQueries = new ArrayList<Query>();
	private List<Query> finalQueries = new ArrayList<Query>();

	private static final int NUMBER_OF_TEMPLATES = 4;
	private static final boolean ENABLE_CUSTOM_QUOTAS = false; //Turn this on to enable the following quotas
	private static final int QUOTA_SLICE_AND_DRILL = 25;
	private static final int QUOTA_SLICE_ALL = 25;
	private static final int QUOTA_EXPLORATIVE = 25;
	//private static final int QUOTA_GOAL_ORIENTED = 25; //not used
	
	private static final int SECOND_LAST_PADDING = 2;
	private static final int MINIMUM_EVOLUTION_DISTANCE = 3;
	private static final int MINIMUM_HIERARCHY_DEPTH = 3;
	private static int NUMBER_OF_CHANGES;
		
	private String name;
	private int numberOfSeedQueries, minSessionLength, maxSessionLength, numberOfSessions;
	private double yearPromptPercentage;
	private boolean segregationPredicate;
	
	private SelectionPredicate segregation;
	private SelectionPredicate yearPrompt;
	
	private Template template;
	
	private Random rand = new Random();
	
	/**
	 * Class Constructor
	 */
	public Profile (String name, int numberOfSeedQueries, int minSessionLength,
					int maxSessionLength, int numberOfSessions,
					double yearPromptPercentage, boolean segregationPredicate)
	{
		this.name = name;
		this.numberOfSeedQueries = numberOfSeedQueries;
		this.minSessionLength = minSessionLength;
		this.maxSessionLength = maxSessionLength;
		this.numberOfSessions = numberOfSessions;
		this.yearPromptPercentage = yearPromptPercentage;
		this.segregationPredicate = segregationPredicate;
		
		NUMBER_OF_CHANGES = (int) (minSessionLength + maxSessionLength) / 2;
	}
	
	/**
	 * This method chooses a Template based on two methods:
	 * if ENABLE_CUSTOM_QUOTAS is turned off, each template has equal 
	 * chance of being picked; otherwise, custom quotas are used.
	 * @return The Template created.
	 */
	private Template chooseTemplate()
	{
		if(ENABLE_CUSTOM_QUOTAS){
			int choice = rand.nextInt(100);
			
			if (choice < QUOTA_SLICE_AND_DRILL)
			{
				return new SliceAndDrill();
			}
			else if (choice >= QUOTA_SLICE_AND_DRILL && choice < (QUOTA_SLICE_AND_DRILL + QUOTA_SLICE_ALL))
			{
				return new SliceAll();
			}
			else if (choice >= (QUOTA_SLICE_AND_DRILL + QUOTA_SLICE_ALL) && choice < (QUOTA_SLICE_AND_DRILL + QUOTA_SLICE_ALL + QUOTA_EXPLORATIVE))
			{
				return new Explorative();
			}
			else 
			{
				return new GoalOriented();
			}
		}
		else{
			int choice = rand.nextInt(NUMBER_OF_TEMPLATES);
			
			if (choice == 0)
			{
				return new SliceAndDrill();
			}
			else if (choice == 1)
			{
				return new SliceAll();
			}
			else if (choice == 2)
			{
				return new Explorative();
			}
			else
			{
				return new GoalOriented();
			}
		}
	}

	/**
	 * This method generates a final query for each of this profile's seed
	 * query (needed for the sessions created by the "Goal Oriented" template).
	 * @param cube The object containing the cube's information and data.
	 */
	private void createFinalQueries(Cube cube)
	{
		for (int count = 0; count < this.seedQueries.size(); ++count)
		{
			Query seedQuery = this.seedQueries.get(count);
			Query currentQuery = Query.clone(seedQuery);
	
			int distance;
	
			do
			{
				for (int iteration = 0; iteration < NUMBER_OF_CHANGES; ++iteration)
				{
					currentQuery = currentQuery.randomEvolution(cube);
				}
				
				distance = currentQuery.distanceFrom(seedQuery, cube);
			} while (distance < MINIMUM_EVOLUTION_DISTANCE);
			
			finalQueries.add(currentQuery);
		}
	}

	/**
	 * This method creates the seed queries from which
	 * all the OLAP sessions of this profile will start.
	 * @param cube The object containing the cube's information and data.
	 * @param maxMeasures The maximum number of measures a seed query can contain.
	 * @param minReportSize The minimum size of a seed query report.
	 * @param maxReportSize The maximum size of a seed query report.
	 */
	private void createSeedQueries(Cube cube, int maxMeasures, int minReportSize, int maxReportSize)
	{
		Level tempLevel;
		boolean choice;
		int reportSize = 1;
		
		if (segregationPredicate)
		{
			setSegregation(cube);
		}
		
		if (yearPromptPercentage > 0)
		{
			setYearPrompt(cube);
		}
		
		int prompted = (int) Math.ceil(numberOfSeedQueries * yearPromptPercentage);
		int notPrompted = numberOfSeedQueries - prompted;
		
		for (int i = 0; i < numberOfSeedQueries; ++i)
		{
			Query newQuery = new Query();
			
			// A level from each hierarchy is added
			for (Hierarchy hie : cube.getHierarchies())
			{
				tempLevel = hie.getRandomLevel(true); // The level can be an ALL level
				newQuery.addGroupByElement(hie.getName(), tempLevel.getName());
			}
			
			// The visibility of the query's Hierarchies is checked
			newQuery.checkValidity(cube);
			
			// A fixed amount of distinct Measures is added
			newQuery.setMeasures(cube.getRandomMeasures(maxMeasures));
			
			if (segregationPredicate) // A segregation predicate must be added
			{				
				newQuery.addSelectionPredicate(segregation);
				Hierarchy segregatedHierarchy = cube.findHierarchy(segregation.getHierarchy());
				int position = segregatedHierarchy.findPosition(segregation.getLevel());
				newQuery.setLevel(segregatedHierarchy.getName(), segregatedHierarchy.getLevel(position - 1).getName());
			}
			
			if (prompted == 0) // All the "year-prompted" queries have been created
			{
				choice = false;
			}
			else if (notPrompted == 0) // All the "non-year-prompted" queries have been created
			{
				choice = true;
			}
			else // Random choice
			{
				choice = rand.nextBoolean();
			}
			
			if (choice) // The year prompt must be added
			{
				newQuery.addSelectionPredicate(yearPrompt);
			}
			
			reportSize = newQuery.getReportSize(cube);
			
			List<Hierarchy> selectableHierarchies = new ArrayList<Hierarchy>();
			
			for (Hierarchy hie : cube.getHierarchies())
			{
				if (! newQuery.containsPredicateOn(hie.getName()))
				{
					String level = newQuery.findLevel(hie.getName());
					int position = hie.findPosition(level);
					
					if (position < hie.getLevelCount() - SECOND_LAST_PADDING)
					{
						selectableHierarchies.add(hie);
					}
				}
			}
			
			// If the report size is too high, a selection predicate is added
			if (selectableHierarchies.size() > 0 && reportSize > maxReportSize)
			{
				Hierarchy hierarchy = selectableHierarchies.get(rand.nextInt(selectableHierarchies.size()));
				String level = newQuery.findLevel(hierarchy.getName());
				int position = hierarchy.findPosition(level);
				
				int predicatePosition = hierarchy.getValidPredicatePosition(position);
				Level predicateLevel = hierarchy.getLevel(predicatePosition);
				
				newQuery.addSelectionPredicate(new SelectionPredicate(hierarchy.getName(),
																	  predicateLevel.getName(),
																	  predicateLevel.getRandomValue(),
																	  false,
																	  false));
				
			}
			
			reportSize = newQuery.getReportSize(cube);
			
			// If the report size is still too high, some hierarchies are rolled-up
			while (reportSize > maxReportSize)
			{
				List<Hierarchy> ascendableHierarchies = new ArrayList<Hierarchy>();
				
				for (Hierarchy hie : cube.getHierarchies())
				{
					if (newQuery.isAscendable(hie))
					{
						ascendableHierarchies.add(hie);
					}
				}
				
				if (ascendableHierarchies.size() == 0)
				{
					break;
				}
				else
				{
					Hierarchy hierarchy = ascendableHierarchies.get(rand.nextInt(ascendableHierarchies.size()));
				
					newQuery.ascendHierarchy(hierarchy);
				
					reportSize = newQuery.getReportSize(cube);
				}
			}
			
			if (reportSize <= maxReportSize && reportSize >= minReportSize)
			{
				seedQueries.add(newQuery);
				
				if (choice)
				{
					--prompted; // One less "year-prompted" query
				}
				else
				{
					--notPrompted; // One less "non-year-prompted" query
				}
			}
			else
			{
				--i; // This query is not valid, cycle is restarted anew
			}
		}
	}

	/**
	 * Getter method for the maximum session length.
	 * @return The maximum session length for this profile.
	 */
	public int getMaxSessionLength()
	{
		return maxSessionLength;
	}

	/**
	 * Getter method for the minimum session length.
	 * @return The minimum session length for this profile.
	 */
	public int getMinSessionLength()
	{
		return minSessionLength;
	}

	/**
	 * Getter method for the profile name.
	 * @return The name of this profile.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Getter method for the number of seed queries.
	 * @return The number of this profile's seed queries.
	 */
	public int getNumberOfQueries()
	{
		return numberOfSeedQueries;
	}
	
	/**
	 * Getter method for the number of sessions.
	 * @return The number of this profile's number of sessions.
	 */
	public int getNumberOfSessions()
	{
		return numberOfSessions;
	}
	
	/**
	 * Getter method for the segregation predicate of year prompt.
	 * @return The presence of segregation for this profile.
	 */
	public boolean getSegregationPredicate()
	{
		return segregationPredicate;
	}

	/**
	 * This method creates all the sessions for the current profile.
	 * @param cube The object containing the cube's information and data.
	 * @param maxMeasures The maximum number of measures for the queries.
	 * @param minReportSize The minimum size for the seed queries reports.
	 * @param maxReportSize The maximum size for the seed queries reports.
	 * @param surprisingQueries The surprising queries 
	 * @return
	 */
	public List<Session> getSessions(Cube cube, int maxMeasures, int minReportSize,
									 int maxReportSize, List<Query> surprisingQueries)
	{
		List<Session> sessionList = new ArrayList<Session>();
		
		createSeedQueries(cube, maxMeasures, minReportSize, maxReportSize);
		createFinalQueries(cube);
		
//		for (int i = 0; i < numberOfSeedQueries; ++i)
//		{
//			for (int j = 1; j <= numberOfSessions; ++j)
//			{
//				template = chooseTemplate();
//				sessionList.add(template.generateSession(name, j, minSessionLength, maxSessionLength,
//														 seedQueries.get(i), cube, surprisingQueries,
//														 finalQueries.get(i)));
//			}
//		}
		
		int i = 0;
		for (int j = 1; j <= numberOfSessions; ++j)
		{
			template = chooseTemplate();
			sessionList.add(template.generateSession(name, j, minSessionLength, maxSessionLength,
													 seedQueries.get(i), cube, surprisingQueries,
													 finalQueries.get(i)));
			System.out.println(seedQueries.get(i).printQuery());
			i = (i == numberOfSeedQueries-1) ? 0 : i+1;
		}
		
		return sessionList;
	}

	/**
	 * Getter method for the percentage of year prompt.
	 * @return The year prompt percentage for this profile.
	 */
	public double getYearPromptPercentage()
	{
		return yearPromptPercentage;
	}
	
	/**
	 * This method builds a segregation predicate, to be added
	 * to all the profile's seed queries. 
	 * @param cube The object containing the cube's information and data.
	 */
	private void setSegregation(Cube cube)
	{
		List<Hierarchy> segregableHierarchies = new ArrayList<Hierarchy>();
		
		for (Dimension dim : cube.getDimensions())
		{
			if (! dim.isTemporal())
			{
				for (Hierarchy hie : dim.getHierarchies())
				{
					if (hie.getLevelCount() >= MINIMUM_HIERARCHY_DEPTH)
					{
						segregableHierarchies.add(hie);
					}
				}
			}
		}
		
		if (segregableHierarchies.size() == 0)
		{
			return;
		}
		
		Hierarchy hierarchy = segregableHierarchies.get(rand.nextInt(segregableHierarchies.size()));
		int position = 1 + rand.nextInt(hierarchy.getLevelCount() - SECOND_LAST_PADDING);
		Level level = hierarchy.getLevel(position);
		
		this.segregation = new SelectionPredicate(hierarchy.getName(),
		   		  								  level.getName(),
		   		  								  level.getRandomValue(),
		   		  								  false,
		   		  								  true);
	}
	
	/**
	 * This method sets an year prompt, to be added to a certain percentage
	 * of the profile's session-seed queries.
	 * @param cube The object containing the cube's information and data.
	 */
	private void setYearPrompt(Cube cube)
	{
		Dimension dimension = cube.getTemporalDimension(); // The temporal dimension is selected
		Hierarchy hierarchy = dimension.getHierarchy(0);
		Level level = hierarchy.getLevel(hierarchy.getLevelCount() - SECOND_LAST_PADDING);
		
		this.yearPrompt = new SelectionPredicate(hierarchy.getName(),
												 level.getName(),
												 level.getRandomValue(),
												 true,
												 false);
	}
}
