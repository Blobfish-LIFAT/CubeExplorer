package it.unibo.csr.big.cubeload.template;
import it.unibo.csr.big.cubeload.schema.Cube;
import it.unibo.csr.big.cubeload.schema.Query;
import it.unibo.csr.big.cubeload.schema.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * This class implements a Template that describes the evolution of an
 * OLAP session. The seed query undergoes a random alteration, then
 * goes through the shortest path towards the most similar surprising
 * query; the session is completed with some other random alterations.
 * @author Luca Spadazzi
 *
 */
public class Explorative implements Template
{
	String name = "Explorative";
	Random rand = new Random();
	
	/**
	 * Getter method for the Template's name.
	 * @return The name of the Template. 
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
	 * @param surprisingQueries The session shall converge to one of these queries.
	 * @param finalQuery - Not used.
	 */
	public Session generateSession(String profileName, int progressive, int minLength,
								   int maxLength, Query seedQuery, Cube cube,
								   List<Query> surprisingQueries, Query finalQuery)
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
	 * @param surprisingQueries The list of global surprising queries.
	 * @param finalQuery -- Not used.
	 * @return The list of queries generated. 
	 */
	private List<Query> getQueryList(int minLength, int maxLength, Query seedQuery, Cube cube,
									List<Query> surprisingQueries, Query finalQuery)
	{
		boolean altered;
		Query lastQuery;
		List<Query> queryList = new ArrayList<Query>();
		int sessionLength = minLength + rand.nextInt(maxLength - minLength + 1);
		
		// First query of the list: seed query
		queryList.add(seedQuery);
		
		// Casual evolution: second query
		queryList.add(seedQuery.randomEvolution(cube));
		
		// Get the surprising query towards which the session will evolve
		Query surprisingQuery = getCloserQuery(queryList.get(1), surprisingQueries, cube);
		
		// Perform all the steps towards the surprising query, until it can't be improved
		do
		{
			lastQuery = Query.clone(queryList.get(queryList.size() - 1));
			altered = lastQuery.convergeTo(surprisingQuery, cube);
			
			// Repetitions must be avoided
			if (altered && !queryList.contains(lastQuery))
			{
				queryList.add(lastQuery);
			}
		} while (altered);
		
		if (queryList.size() < sessionLength) // If the session is not long enough
		{
			int queryToAdd = sessionLength - queryList.size();
			
			for (int count = 0; count < queryToAdd; ++count)
			{
				lastQuery = Query.clone(queryList.get(queryList.size() - 1));
				Query newQuery = lastQuery.randomEvolution(cube);
				// Repetitions must be avoided
				if (!queryList.contains(newQuery)) {
					queryList.add(newQuery);
				}
				else {
					count--;
				}
			}
		}
		else if (queryList.size() > sessionLength) // If the session is too long
		{
			int queryToRemove = queryList.size() - sessionLength;
			int index;
			
			for (int count = 0; count < queryToRemove; ++count)
			{
				index = 2 + rand.nextInt(queryList.size() - 3);
				queryList.remove(index);
			}
		}
		
		return queryList;
	}

	/**
	 * This method checks which surprising query is closest to the starting query.
	 * @param seedQuery The starting query to compare with the surprising queries.
	 * @param surprisingQueries The list of queries to compare with the seed query given.
	 * @param cube The object containing the cube's information and data.
	 * @return The surprising query closest to the starting query.
	 */
	private Query getCloserQuery(Query seedQuery, List<Query> surprisingQueries, Cube cube)
	{
		int minimumDistance = seedQuery.distanceFrom(surprisingQueries.get(0), cube);
		int temp, index = 0;
		
		for (int count = 1; count < surprisingQueries.size(); ++count)
		{
			temp = seedQuery.distanceFrom(surprisingQueries.get(count), cube);
			
			if (temp < minimumDistance)
			{
				minimumDistance = temp;
				index = count;
			}
		}
		
		return surprisingQueries.get(index);
	}
}