package it.unibo.csr.big.cubeload.schema;



import java.util.ArrayList;
import java.util.List;


/**
 * This class represents an implementation of an OLAP query session.
 * A session belongs to a profile, and is created by a template
 * (which generates the session's query list).
 * @author Luca Spadazzi
 *
 */
public class Session
{
	/**
	 * Class fields.
	 */
	private String profileName;
	private int progressive;
	private List<Query> queryList = new ArrayList<Query>();
	private String templateName;

	/**
	 * Class constructor.
	 * @param profileName The name of the profile that creates this session.
	 * @param progressive The progressive number of this session.
	 * @param templateName The name of the template that
	 */
	public Session(String profileName, int progressive, String templateName)
	{
		this.profileName = profileName;
		this.progressive = progressive;
		this.templateName = templateName;
	}
	
	/**
	 * Getter method for the name of the session's profile.
	 * @return The session's profile name.
	 */
	public String getProfileName()
	{
		return profileName;
	}

	/**
	 * Getter method for the progressive number of the session.
	 * @return The session's progressive number.
	 */
	public int getProgressive()
	{
		return progressive;
	}

	/**
	 * Getter method for the list of queries of the session.
	 * @return The session's query list.
	 */
	public List<Query> getQueryList()
	{
		return queryList;
	}

	/**
	 * Getter method for the name of the session's template.
	 * @return The name of the template that creates this session.
	 */
	public String getTemplateName()
	{
		return templateName;
	}

	/**
	 * Setter method for the list of queries of the session.
	 * @param queryList The session's query list.
	 */
	public void setQueryList(List<Query> queryList)
	{
		this.queryList = queryList;
	}
}
