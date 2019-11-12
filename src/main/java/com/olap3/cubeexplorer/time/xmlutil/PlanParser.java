package com.olap3.cubeexplorer.time.xmlutil;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PlanParser {

    public static XMLPlan xml_to_plan(String xml) throws DocumentException {

        StringReader stringReader = new StringReader(xml);
        SAXReader saxReader = new SAXReader();
        Document doc = saxReader.read(stringReader);

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
            est_rows = Math.round(Double.valueOf(val_rows));
        }

        //Alex stuff :
        XPath relopP = doc.createXPath("//ms:RelOp");
        relopP.setNamespaceURIs(map);
        double full_cost = 0;
        for (Node n : relopP.selectNodes(doc)){
            Element e = (Element) n;
            full_cost += Double.parseDouble(e.attributeValue("EstimateRows"));
        }

        return new XMLPlan(est_rows,-1, total_cost, Math.round(full_cost));
    }


    public static void main(String[] args) throws DocumentException, IOException {

        File file = new File("data/example_plan.xml");

        String content = Files.readAllLines(file.toPath()).stream().collect(Collectors.joining());

        System.out.println(content);

        XMLPlan plan = PlanParser.xml_to_plan(content);

        System.out.println(plan.toString());


    }

}
