package com.olap3.cubeexplorer.xmlutil;

import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PlanParser {


    public static class Plan {
        public final long estimated_tuples;
        public final long estimated_time;
        public final double total_cost;

        public Plan(long estimated_tuples, long estimated_time, double total_cost) {
            this.estimated_tuples = estimated_tuples;
            this.estimated_time = estimated_time;
            this.total_cost = total_cost;
        }

        @Override
        public String toString() {
            return String.format("estimated tuples: %d%nestimated cost: %f", this.estimated_tuples, this.total_cost);
        }
    }


    public static Plan xml_to_plan(String xml) throws DocumentException {

        StringReader stringReader = new StringReader(xml);

        SAXReader saxReader = new SAXReader();

        Document doc = saxReader.read(stringReader);

        Namespace namespace = new Namespace("ms", "http://schemas.microsoft.com/sqlserver/2004/07/showplan" );

        //Node stmt = doc.selectSingleNode("//StmtSimple");

        XPath xPath = doc.createXPath("//ms:StmtSimple");

        Map<String, String> map = new HashMap<>();

        map.put("ms","http://schemas.microsoft.com/sqlserver/2004/07/showplan");

        xPath.setNamespaceURIs(map);

        Element stmt = (Element) xPath.selectSingleNode(doc);

        String val_cost = stmt.attributeValue("StatementSubTreeCost");

        double total_cost = -1.0;

        if (val_cost != null && !val_cost.equals("")) {
            total_cost = Double.valueOf(val_cost);
        }

        String val_rows = stmt.attributeValue("StatementEstRows");

        long est_rows = -1;

        if (val_rows != null && !val_rows.equals("")) {
            est_rows = Long.valueOf(val_rows);
        }

        return new Plan(est_rows,-1, total_cost);
    }


    public static void main(String[] args) throws DocumentException, IOException {

        File file = new File("/home/ben/Documents/showplan.xml");

        String content = Files.readAllLines(file.toPath()).stream().collect(Collectors.joining());

        System.out.println(content);

        Plan plan = PlanParser.xml_to_plan(content);

        System.out.println(plan.toString());


    }

}
