package it.unibo.csr.big.cubeload.io;

import it.unibo.csr.big.cubeload.generator.Profile;
import it.unibo.csr.big.cubeload.schema.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;


/**
 * This class explores an XML file, containing the structure of a cube's schema.
 * All the dimensions, hierarchies, levels and measures are added in a newly
 * created cube object
 * @author Luca Spadazzi
 *
 */
public class XMLReader
{
	public Cube getCube (String source, String cubeName) throws Exception
	{
		XPathFactory xpf = XPathFactory.newInstance();
		XPath xPath = xpf.newXPath();
		String path = "/Schema/Cube[@name='" + cubeName + "']";
		String dimensionPath = path + "/Dimension";
		String dimensionUsagePath = path + "/DimensionUsage";
		String measurePath = path + "/Measure";
		List<Dimension> dimList = new ArrayList<Dimension>();
		InputSource inputSource = new InputSource(source);
		
		NodeList globalDimensions = (NodeList) xPath.evaluate("/Schema/Dimension", inputSource, XPathConstants.NODESET);
		
		// Retrieving information for the global dimensions
		for (int i = 0; i < globalDimensions.getLength(); ++i)
		{
			Node dimNode = globalDimensions.item(i);
			String dimName = xPath.evaluate("./@name", dimNode);

			Dimension dimension = new Dimension(dimName);
			
			if (dimName.toUpperCase().equals("TIME") ||
				dimName.toUpperCase().equals("YEAR"))
			{
				dimension.setTemporal(true);
			}
			else
			{
				dimension.setTemporal(false);
			}
			
			NodeList hierarchies = (NodeList) xPath.evaluate("./Hierarchy", dimNode, XPathConstants.NODESET);

			for (int j = 0; j < hierarchies.getLength(); ++j)
			{
				Node hieNode = hierarchies.item(j);
				String hieName;
				
				if (j == 0)
				{
					hieName = dimName;
				}
				else
				{
					hieName = xPath.evaluate("./@name", hieNode);
				}
				
				Hierarchy hierarchy = new Hierarchy(hieName);
				
				hierarchy.addLevel(Level.newLevel("ALL" + hieName));
				
				NodeList levels = (NodeList) xPath.evaluate("./Level", hieNode, XPathConstants.NODESET);
				
				for (int k = 0; k < levels.getLength(); ++k)
				{
					Node lvlNode = levels.item(k);
					String lvlName = xPath.evaluate("./@name", lvlNode);

					hierarchy.addLevel(Level.newLevel(lvlName));
				}
				
				hierarchy.getLevel(hierarchy.getLevelCount() - 1).addDistinctValues("ALL" + hieName);
				
				dimension.addHierarchy(hierarchy);
			}
			
			dimList.add(dimension);
		}
		
		//NodeList cubeNode = (NodeList) xPath.evaluate(path, inputSource, XPathConstants.NODESET);
		Cube cube = new Cube(cubeName);
		
		NodeList dimensionsUsage = (NodeList) xPath.evaluate(dimensionUsagePath, inputSource, XPathConstants.NODESET);
		
		// Retrieving information for this cube's global dimensions
		for (int i = 0; i < dimensionsUsage.getLength(); ++i)
		{
			Node dimNode = dimensionsUsage.item(i);
			String dimName = xPath.evaluate("./@name", dimNode);
			
			for (Dimension dim : dimList)
			{
				if (dim.getName().equals(dimName))
				{
					cube.addDimension(dim);
				}
			}
		}
		
		NodeList dimensions = (NodeList) xPath.evaluate(dimensionPath, inputSource, XPathConstants.NODESET);
		
		// Retrieving information for this cube's local dimensions
		for (int i = 0; i < dimensions.getLength(); ++i)
		{
			Node dimNode = dimensions.item(i);
			String dimName = xPath.evaluate("./@name", dimNode);

			Dimension dimension = new Dimension(dimName);
			
			if (dimName.toUpperCase().equals("TIME") ||
				dimName.toUpperCase().equals("YEAR"))
			{
				dimension.setTemporal(true);
			}
			else
			{
				dimension.setTemporal(false);
			}
			
			NodeList hierarchies = (NodeList) xPath.evaluate("./Hierarchy", dimNode, XPathConstants.NODESET);

			for (int j = 0; j < hierarchies.getLength(); ++j)
			{
				Node hieNode = hierarchies.item(j);
				String hieName;
				
				if (j == 0)
				{
					hieName = dimName;
				}
				else
				{
					hieName = xPath.evaluate("./@name", hieNode);
				}
				
				Hierarchy hierarchy = new Hierarchy(hieName);
				
				hierarchy.addLevel(Level.newLevel("ALL" + hieName));
				
				NodeList levels = (NodeList) xPath.evaluate("./Level", hieNode, XPathConstants.NODESET);
				
				for (int k = 0; k < levels.getLength(); ++k)
				{
					Node lvlNode = levels.item(k);
					String lvlName = xPath.evaluate("./@name", lvlNode);

					hierarchy.addLevel(Level.newLevel(lvlName));
				}
				
				hierarchy.getLevel(hierarchy.getLevelCount() - 1).addDistinctValues("ALL" + hieName);
				
				dimension.addHierarchy(hierarchy);
			}
			
			cube.addDimension(dimension);
		}
		
		// Retrieving information for this cube's measures
		NodeList measures = (NodeList) xPath.evaluate(measurePath, inputSource, XPathConstants.NODESET);
		
		for (int i = 0; i < measures.getLength(); ++i)
		{
			Node measNode = measures.item(i);
			String measName = xPath.evaluate("./@name", measNode);
			
			cube.addMeasure(new Measure(measName));
		}
		
		return cube;
	}
	
	/**
	 * This method scans a source (XML file) and saves in a list all the
	 * names of the cubes in the schema.
	 * @return The list of cube names found.
	 * @throws Exception
	 */
	public List<String> getCubeNames(String source) throws Exception
	{
		XPathFactory xpf = XPathFactory.newInstance();
		XPath xPath = xpf.newXPath();
		List<String> cubeNames = new ArrayList<String>();
		
		InputSource inputSource = new InputSource(source);
		
		NodeList cubes = (NodeList) xPath.evaluate("/Schema/Cube", inputSource, XPathConstants.NODESET);
		
		for (int i = 0; i < cubes.getLength(); ++i)
		{
			Node cubeNode = cubes.item(i);
			cubeNames.add(xPath.evaluate("./@name", cubeNode));
		}
		
		return cubeNames;
	}
	
	/**
	 * This method builds a profile reading an XML file containing its parameters.
	 * @param source The canonical path of the XML file.
	 * @return The profile created.
	 * @throws Exception
	 */
	public Profile getProfile (String source) throws Exception
	{
		XPathFactory xpf = XPathFactory.newInstance();
		XPath xPath = xpf.newXPath();
		InputSource inputSource = new InputSource(source);
		
		String name = xPath.evaluate("/Profile/Name/@value", inputSource);
		int numQueries = Integer.valueOf(xPath.evaluate("/Profile/NumberOfSessionSeedQueries/@value", inputSource));
		int minLength = Integer.valueOf(xPath.evaluate("/Profile/MinSessionLength/@value", inputSource));
		int maxLength = Integer.valueOf(xPath.evaluate("/Profile/MaxSessionLength/@value", inputSource));
		int numSessions = Integer.valueOf(xPath.evaluate("/Profile/NumberOfSessions/@value", inputSource));
		double yearPrompt = Double.valueOf(xPath.evaluate("/Profile/YearPrompt/@value", inputSource));
		boolean segregation = Boolean.valueOf(xPath.evaluate("/Profile/SegregationPredicate/@value", inputSource));
		
		return new Profile(name, numQueries, minLength, maxLength, numSessions,
						   yearPrompt, segregation);
	}
}
