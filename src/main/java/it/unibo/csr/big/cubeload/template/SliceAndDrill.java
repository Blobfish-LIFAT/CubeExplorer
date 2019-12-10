package it.unibo.csr.big.cubeload.template;

import it.unibo.csr.big.cubeload.schema.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * This class implements a Template that describes the evolution of
 * an OLAP session. The seed query undergoes a complete drill-down
 * on a hierarchy with subsequent selection predicates; if the session
 * is too short, another hierarchy is drilled. 
 * @author Luca Spadazzi
 *
 */
public class SliceAndDrill implements Template
{
	private static final int SECOND_LAST_PADDING = 2;
	private static final int MINIMUM_HIERARCHY_DEPTH = 3;
	String name = "Slice and Drill";
	Random rand = new Random();
	
	/**
	 * Getter method for the template name.
	 * @return The name of the template.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * This method creates a fully-formed session, based on the parameters passed.
	 * @param profileName The name of the profile to which the session belongs.
	 * @param progressive The session's progressive number.
	 * @param minLength The session length's lower bound.
	 * @param maxLength The session length's upper bound.
	 * @param seedQuery The session's starting query.
	 * @param cube The object containing the cube's information and data.
	 * @param surprisingQueries - Not used.
	 * @param finalQuery - Not used.
	 */
	public Session generateSession(String profileName, int progressive, int minLength, int maxLength, Query seedQuery,
			   Cube cube, List<Query> surprisingQueries, Query finalQuery)
	{
		Session session = new Session(profileName, progressive, this.name);
		
		session.setQueryList(getQueryList(minLength, maxLength, seedQuery, cube, surprisingQueries, finalQuery));
		
		return session;
	}
	
	/**
	 * This method generates a list of queries that will form a session.
	 * @param minLength The minimum session length.
	 * @param maxLength The maximum session length.
	 * @param seedQuery The starting query of the session.
	 * @param cube The object containing the cube's information and data.
	 * @param surprisingQueries -- Not used.
	 * @param finalQuery -- Not used.
	 * @return The list of queries generated. 
	 */
	private List<Query> getQueryList(int minLength, int maxLength, Query seedQuery, Cube cube,
									List<Query> surprisingQueries, Query finalQuery)
	{
		List<Query> queryList = new ArrayList<Query>();
		List<Hierarchy> drilledHierarchies = new ArrayList<Hierarchy>();
		List<Hierarchy> drillableHierarchies = new ArrayList<Hierarchy>();
		int sessionLength = minLength + rand.nextInt(maxLength - minLength + 1);
		
		// I look for non-segregated hierarchies with enough levels
		for (Hierarchy hierarchy : cube.getHierarchies())
		{
			if (! seedQuery.isSegregatedOn(hierarchy) &&
				hierarchy.getLevelCount() >= MINIMUM_HIERARCHY_DEPTH)
			{
				drillableHierarchies.add(hierarchy);
			}
		}
		
		queryList.add(seedQuery);
		
		while (queryList.size() < sessionLength)
		{
			boolean alreadyMaxLevel = false;
			String levelName;
			Query newQuery = Query.clone(seedQuery);
			
			if (drillableHierarchies.size() == 0)
			{
				break;
			}
			
			Hierarchy hie = drillableHierarchies.get(rand.nextInt(drillableHierarchies.size()));
			
			drilledHierarchies.add(hie); // The hierarchy will be drilled
			drillableHierarchies.remove(hie); // The hierarchy can't be drilled again
			
			// If the seed query, in this hierarchy, has the second-to-last level of aggregation,
			// the following changes are superfluous: thus, the query goes directly through drilling
			if (hie.findPosition(newQuery.findLevel(hie.getName())) == hie.getLevelCount() - SECOND_LAST_PADDING)
			{
				alreadyMaxLevel = true;
			}
			
			// I remove the predicate on the chosen hierarchy (if any)
			if (newQuery.containsPredicateOn(hie.getName()))
			{
				SelectionPredicate selPred = newQuery.getSelectionPredicate(hie.getName());
				
				for (SelectionPredicate sel : newQuery.getPredicates())
				{
					if (sel.getHierarchy().equals(selPred.getHierarchy()))
					{
						newQuery.getPredicates().remove(sel);
						break;
					}
				}
			}
			
			if (! alreadyMaxLevel)
			{
				levelName = hie.getLevel(hie.getLevelCount() - SECOND_LAST_PADDING).getName();
				newQuery.setLevel(hie.getName(), levelName);
				// Repetitions must be avoided
				if (!queryList.contains(newQuery)) {
					queryList.add(newQuery);
				}
				newQuery = Query.clone(queryList.get(queryList.size() - 1));
			}
			
			while (newQuery.isDescendible(hie))
			{
				// I remove the predicate on the chosen hierarchy (if any)
				if (newQuery.containsPredicateOn(hie.getName()))
				{
					SelectionPredicate selPred = newQuery.getSelectionPredicate(hie.getName());
					
					for (SelectionPredicate sel : newQuery.getPredicates())
					{
						if (sel.getHierarchy().equals(selPred.getHierarchy()))
						{
							newQuery.getPredicates().remove(sel);
							break;
						}
					}
				}
				
				// I obtain the current level in the hierarchy
				levelName = newQuery.findLevel(hie.getName());
				Level currentLevel = hie.getLevel(levelName);
				
				// I add a selection predicate on the current level
				newQuery.addSelectionPredicate(new SelectionPredicate(hie.getName(),
															   		  currentLevel.getName(),
															   		  currentLevel.getRandomValue(),
															   		  false,
															   		  false));
				
				// I descend the hierarchy
				newQuery.descendHierarchy(hie);
				
				// The query is added to the list (repetitions must be avoided)
				if (!queryList.contains(newQuery)) {
					queryList.add(newQuery);
				}
				
				newQuery = Query.clone(queryList.get(queryList.size() - 1));
			}
		}
		
		// If the session is too long, only the first 'maxLength' queries are retained
		if (queryList.size() > sessionLength)
		{
			queryList = queryList.subList(0, sessionLength);
		}
		
		return queryList;
	}
}
