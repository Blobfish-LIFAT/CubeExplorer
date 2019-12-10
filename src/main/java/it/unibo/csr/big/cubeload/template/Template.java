package it.unibo.csr.big.cubeload.template;
import it.unibo.csr.big.cubeload.schema.Cube;
import it.unibo.csr.big.cubeload.schema.Query;
import it.unibo.csr.big.cubeload.schema.Session;

import java.util.List;


/**
 * This interface lists the methods that every template must implement:
 * a getter method for the template name, and the method to generate
 * and return the list of queries that will form a session.
 * @author Luca Spadazzi
 *
 */
public interface Template
{
	public Session generateSession(String profileName, int progressive, int minLength,
                                   int maxLength, Query seedQuery, Cube cube,
                                   List<Query> surprisingQueries, Query finalQuery);
	
	public String getName();
}
