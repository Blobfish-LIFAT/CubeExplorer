package it.unibo.csr.big.cubeload.template;

import it.unibo.csr.big.cubeload.schema.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


/**
 * This class implements a Template that describes the evolution of
 * an OLAP session. The seed query is continually selected on
 * an arbitrary hierarchy level, with a drill-down if necessary. 
 * @author Luca Spadazzi
 *
 */
public class SliceAll implements Template
{
	private static final int SECOND_LAST_PADDING = 2;
	private static final int MINIMUM_HIERARCHY_DEPTH = 4;
	
	private String name = "Slice All";
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
		List<Query> queryList = new ArrayList<Query>(); // This list will contain the session queries
		String removedValue = null;
		int sessionLength = minLength + rand.nextInt(maxLength - minLength + 1);
		
		queryList.add(seedQuery); // The seed query must be the first element of the list
		
		List<Hierarchy> sliceableHierarchies = new ArrayList<Hierarchy>();
		
		for (Hierarchy hie : cube.getHierarchies())
		{
			if (! queryList.get(0).isSegregatedOn(hie) && hie.getLevelCount() >= MINIMUM_HIERARCHY_DEPTH)
			{
				sliceableHierarchies.add(hie);
			}
		}
		
		Hierarchy slicedHierarchy = sliceableHierarchies.get(rand.nextInt(sliceableHierarchies.size()));
		
		// Select an intermediate level
		Level predicateLevel = slicedHierarchy.getLevel(2 + rand.nextInt(slicedHierarchy.getLevelCount() - SECOND_LAST_PADDING - 1));
		
		Level queryLevel = slicedHierarchy.getLevel(slicedHierarchy.findPosition(predicateLevel.getName()) - 1);
		
		// I get the level's distinct values
		List<String> valuesList = predicateLevel.getValues();
		
		// Random shift of the distinct values
		Collections.rotate(valuesList, rand.nextInt(valuesList.size()));
		
		Query query = Query.clone(queryList.get(0)); // I get the seed query
		
		// If it contains a selection predicate, it is removed
		if (query.containsPredicateOn(slicedHierarchy.getName()))
		{
			SelectionPredicate selPred = query.getSelectionPredicate(slicedHierarchy.getName());
			removedValue = selPred.getElement();
			query.getPredicates().remove(selPred);
		}
		
		valuesList.remove(removedValue);
		
		query.setLevel(slicedHierarchy.getName(), queryLevel.getName());
		
		// I add a selection predicate on the chosen hierarchy and level
		query.addSelectionPredicate(new SelectionPredicate(slicedHierarchy.getName(),
														   predicateLevel.getName(),
														   valuesList.get(0),
														   false,
														   false));
		
		// The new query is added to the list
		queryList.add(query);
		
		// The added selection predicate is replaced in every query
		for (int count = 1; count < valuesList.size(); ++count)
		{
			query = Query.clone(queryList.get(queryList.size() - 1));
			replacePredicate(query, slicedHierarchy, valuesList.get(count));
			queryList.add(query);			
		}
		
		// The list will contain a random acceptable number of queries
		if (queryList.size() < sessionLength)
		{
			// The last query generated will be the base for the next series
			Query newQuery = Query.clone(queryList.get(queryList.size() - 1));
			
			// The selection predicate on the current hierarchy shall be removed
			SelectionPredicate selPred = newQuery.getSelectionPredicate(slicedHierarchy.getName()); 
			newQuery.getPredicates().remove(selPred);
			
			// A drill-down is performed on the current hierarchy (possible, see above)
			newQuery.descendHierarchy(slicedHierarchy);
			
			int position = slicedHierarchy.findPosition(newQuery.findLevel(slicedHierarchy.getName()));
			Level level = slicedHierarchy.getLevel(position + 1);
			valuesList = level.getValues();
			valuesList.remove(removedValue);
			Collections.rotate(valuesList, rand.nextInt(valuesList.size()));
			
			newQuery.addSelectionPredicate(new SelectionPredicate(slicedHierarchy.getName(),
														   		  level.getName(),
														   		  valuesList.get(0),
														   		  false,
														   		  false));
			
			queryList.add(newQuery);
			
			for (int count = 1; count < valuesList.size(); ++count)
			{
				newQuery = Query.clone(queryList.get(queryList.size() - 1));
				replacePredicate(newQuery, slicedHierarchy, valuesList.get(count));
				queryList.add(newQuery);			
			}
		}
		
		if (queryList.size() > sessionLength)
		{
			queryList = queryList.subList(0, sessionLength);
		}
		
		return queryList;
	}
	
	/**
	 * This method alters a predicate on the given hierarchy, changing the value of its selection.
	 * @param query The query on which the replace must be done.
	 * @param hierarchy The hierarchy whose selection predicate must be changed
	 * @param value The new value of the selection predicate.
	 */
	private void replacePredicate(Query query, Hierarchy hierarchy, String value)
	{
		SelectionPredicate selPred = query.getSelectionPredicate(hierarchy.getName());
		SelectionPredicate newSelPred = new SelectionPredicate(selPred.getHierarchy(),
															   selPred.getLevel(),
															   value,
															   selPred.getPrompt(),
															   selPred.getSegregation());
		
		query.getPredicates().remove(selPred);
		query.addSelectionPredicate(newSelPred);
	}
}