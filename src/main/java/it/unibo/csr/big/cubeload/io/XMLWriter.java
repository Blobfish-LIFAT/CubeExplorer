package it.unibo.csr.big.cubeload.io;

import it.unibo.csr.big.cubeload.generator.Profile;
import it.unibo.csr.big.cubeload.schema.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;


public class XMLWriter {
    /**
     * This method builds and saves an XML file with the workload produced.
     * Data include date/time, global parameters, profile parameters and sessions.
     *
     * @param numberOfProfiles          The number of profiles simulated.
     * @param maxMeasures               The maximum number of measures per query.
     * @param minReportSize             The minimum size of session-seed queries reports.
     * @param maxReportSize             The maximum size of session-seed queries reports.
     * @param numberOfSurprisingQueries The number of surprising queries.
     * @param profileList               The profiles simulated.
     * @param sessionList               The sessions produced.
     */
    public static void XMLGenerator(String path, int numberOfProfiles, int maxMeasures, int minReportSize,
                                    int maxReportSize, int numberOfSurprisingQueries,
                                    List<Profile> profileList, List<Session> sessionList) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // Root element
            Document doc = docBuilder.newDocument();
            Element benchmark = doc.createElement("Benchmark");
            doc.appendChild(benchmark);

            // Date and time
            Element date = doc.createElement("Date-Time");
            date.appendChild(doc.createTextNode(new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime())));
            benchmark.appendChild(date);

            // Global parameters
            Element globalParameters = doc.createElement("GlobalParameters");
            benchmark.appendChild(globalParameters);

            Element numOfProfiles = doc.createElement("NumberOfProfiles");
            numOfProfiles.setAttribute("value", Integer.toString(numberOfProfiles));
            globalParameters.appendChild(numOfProfiles);

            Element maxMeas = doc.createElement("MaxMeasures");
            maxMeas.setAttribute("value", Integer.toString(maxMeasures));
            globalParameters.appendChild(maxMeas);

            Element minRepSize = doc.createElement("MinReportSize");
            minRepSize.setAttribute("value", Integer.toString(minReportSize));
            globalParameters.appendChild(minRepSize);

            Element maxRepSize = doc.createElement("MaxReportSize");
            maxRepSize.setAttribute("value", Integer.toString(maxReportSize));
            globalParameters.appendChild(maxRepSize);

            Element surpQueries = doc.createElement("SurprisingQueries");
            surpQueries.setAttribute("value", Integer.toString(numberOfSurprisingQueries));
            globalParameters.appendChild(surpQueries);

            // Profile parameters
            Element profileParameters = doc.createElement("ProfileParameters");
            benchmark.appendChild(profileParameters);

            for (int i = 0; i < profileList.size(); ++i) {
                Element profile = doc.createElement("Profile");
                profile.setAttribute("progressive", Integer.toString(i + 1));

                Element profileName = doc.createElement("Name");
                profileName.setAttribute("value", profileList.get(i).getName());
                profile.appendChild(profileName);

                Element seedQueries = doc.createElement("SeedQueries");
                seedQueries.setAttribute("value", Integer.toString(profileList.get(i).getNumberOfQueries()));
                profile.appendChild(seedQueries);

                Element minSession = doc.createElement("MinSessionLength");
                minSession.setAttribute("value", Integer.toString(profileList.get(i).getMinSessionLength()));
                profile.appendChild(minSession);

                Element maxSession = doc.createElement("MaxSessionLength");
                maxSession.setAttribute("value", Integer.toString(profileList.get(i).getMaxSessionLength()));
                profile.appendChild(maxSession);

                Element numOfSessions = doc.createElement("NumberOfSessions");
                numOfSessions.setAttribute("value", Integer.toString(profileList.get(i).getNumberOfSessions()));
                profile.appendChild(numOfSessions);

                Element yearPrompt = doc.createElement("YearPrompt");
                yearPrompt.setAttribute("value", Double.toString(profileList.get(i).getYearPromptPercentage()));
                profile.appendChild(yearPrompt);

                Element segregation = doc.createElement("SegregationPredicate");
                segregation.setAttribute("value", String.valueOf(profileList.get(i).getSegregationPredicate()));
                profile.appendChild(segregation);

                profileParameters.appendChild(profile);
            }

            // Sessions

            for (int i = 0; i < sessionList.size(); ++i) {
                Session currentSession = sessionList.get(i);

                Element session = doc.createElement("Session");
                session.setAttribute("profile", currentSession.getProfileName());
                session.setAttribute("progressive", Integer.toString(i + 1));
                session.setAttribute("template", currentSession.getTemplateName());

                benchmark.appendChild(session);

                for (int j = 0; j < currentSession.getQueryList().size(); ++j) {
                    Query currentQuery = currentSession.getQueryList().get(j);

                    Element query = doc.createElement("Query");
                    query.setAttribute("progressive", Integer.toString(j + 1));

                    session.appendChild(query);

                    Element groupBy = doc.createElement("GroupBy");
                    query.appendChild(groupBy);

                    for (GroupByElement gb : currentQuery.getGroupBySet()) {
                        if (gb.getVisible()) {
                            Element element = doc.createElement("Element");
                            groupBy.appendChild(element);

                            Element hierarchy = doc.createElement("Hierarchy");
                            hierarchy.setAttribute("value", gb.getHierarchy());
                            element.appendChild(hierarchy);

                            Element level = doc.createElement("Level");
                            level.setAttribute("value", gb.getLevel());
                            element.appendChild(level);
                        }
                    }

                    Element measures = doc.createElement("Measures");
                    query.appendChild(measures);

                    for (Measure meas : currentQuery.getMeasures()) {
                        Element element = doc.createElement("Element");
                        element.setAttribute("value", meas.getName());

                        measures.appendChild(element);
                    }

                    Element selectionPredicates = doc.createElement("SelectionPredicates");
                    query.appendChild(selectionPredicates);

                    for (SelectionPredicate selPred : currentQuery.getPredicates()) {
                        Element element = doc.createElement("Element");
                        selectionPredicates.appendChild(element);

                        Element hierarchy = doc.createElement("Hierarchy");
                        hierarchy.setAttribute("value", selPred.getHierarchy());
                        element.appendChild(hierarchy);

                        Element level = doc.createElement("Level");
                        level.setAttribute("value", selPred.getLevel());
                        element.appendChild(level);

                        Element predicate = doc.createElement("Predicate");
                        predicate.setAttribute("value", selPred.getElement());
                        element.appendChild(predicate);

                        Element yearPrompt = doc.createElement("YearPrompt");
                        yearPrompt.setAttribute("value", String.valueOf(selPred.getPrompt()));
                        element.appendChild(yearPrompt);

                        Element segregation = doc.createElement("SegregationPredicate");
                        segregation.setAttribute("value", String.valueOf(selPred.getSegregation()));
                        element.appendChild(segregation);
                    }
                }
            }

            // Write the content into an XML file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(path));

            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (ParserConfigurationException | TransformerException pce) {
            pce.printStackTrace();
        }
	}

    /**
     * This method builds and saves an XML file with the parameters of a single profile.
     *
     * @param name        The profile's name.
     * @param numQueries  The number of session-seed queries.
     * @param minLength   The minimum session length.
     * @param maxLength   The maximum session length.
     * @param numSessions The number of sessions.
     * @param yearPrompt  The percentage of this profile's year prompt.
     * @param segregation The flag for the presence of a segregation predicate.
     */
    public static void saveProfile(String name,
                            int numQueries,
                            int minLength,
                            int maxLength,
                            int numSessions,
                            double yearPrompt,
                            boolean segregation) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element profileElement = doc.createElement("Profile");
            doc.appendChild(profileElement);

            Element nameElement = doc.createElement("Name");
            nameElement.setAttribute("value", name);
            profileElement.appendChild(nameElement);

            Element seedQueries = doc.createElement("NumberOfSessionSeedQueries");
            seedQueries.setAttribute("value", Integer.toString(numQueries));
            profileElement.appendChild(seedQueries);

            Element minSession = doc.createElement("MinSessionLength");
            minSession.setAttribute("value", Integer.toString(minLength));
            profileElement.appendChild(minSession);

            Element maxSession = doc.createElement("MaxSessionLength");
            maxSession.setAttribute("value", Integer.toString(maxLength));
            profileElement.appendChild(maxSession);

            Element numSession = doc.createElement("NumberOfSessions");
            numSession.setAttribute("value", Integer.toString(numSessions));
            profileElement.appendChild(numSession);

            Element yearPromptElement = doc.createElement("YearPrompt");
            yearPromptElement.setAttribute("value", Double.toString(yearPrompt));
            profileElement.appendChild(yearPromptElement);

            Element segregationElement = doc.createElement("SegregationPredicate");
            segregationElement.setAttribute("value", String.valueOf(segregation));
            profileElement.appendChild(segregationElement);

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(name + ".xml"));

            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }
}
