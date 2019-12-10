package it.unibo.csr.big.cubeload.template;
import it.unibo.csr.big.cubeload.schema.Cube;
import it.unibo.csr.big.cubeload.schema.Query;
import it.unibo.csr.big.cubeload.schema.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * This class implements a Template that describes the evolution of an
 * OLAP session. The seed query walks a shortest path towards a
 * predetermined final query; if the session isn't long enough, an
 * internal query q is replaced by the sequence {q, q*}, which
 * represents a deviation from the path. 
 * @author Luca Spadazzi
 *
 */
public class GoalOriented implements Template
{
	String name = "Goal Oriented";
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
	 * @param finalQuery The last query of this session.
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
	 * @param surprisingQueries -- Not used.
	 * @param finalQuery The terminal query of the session.
	 * @return The list of queries generated. 
	 */
	private List<Query> getQueryList(int minLength, int maxLength, Query seedQuery, Cube cube,
									List<Query> surprisingQueries, Query finalQuery)
	{
		List<Query> queryList = new ArrayList<Query>();
		Query newQuery, alteredQuery;
		Boolean altered;
		int sessionLength = minLength + rand.nextInt(maxLength - minLength + 1);
		
		queryList.add(seedQuery);

		do
		{
			newQuery = Query.clone(queryList.get(queryList.size() - 1));
			altered = newQuery.convergeTo(finalQuery, cube);
			
			if (altered)
			{
				queryList.add(newQuery);
			}
		} while (altered);
		
		// If the list isn't long enough, queries shall be added
		while (queryList.size() < sessionLength)
		{
			// Avoid alterations near the end of the session
			int position = rand.nextInt( (int)Math.floor(queryList.size() * 0.6) );
			newQuery = Query.clone(queryList.get(position));
			alteredQuery = newQuery.randomEvolution(cube);
			
			// Repetitions must be avoided
			if (!queryList.contains(alteredQuery)) {
				queryList.add(position, alteredQuery);
			}
		}
		
		// If the list was (or has become) too long, queries shall be removed
		if (queryList.size() > sessionLength)
		{
			int queryToRemove = queryList.size() - maxLength;
			
			for (int count = 0; count < queryToRemove; ++count)
			{
				int position = 1 + rand.nextInt(queryList.size() - 2);
				queryList.remove(position);
			}
		}
		
		return queryList;
	}
}