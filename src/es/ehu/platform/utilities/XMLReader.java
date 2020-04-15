package es.ehu.platform.utilities;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import java.util.ArrayList;

public class XMLReader {

    public static ArrayList<ArrayList<ArrayList<String>>> readFile (String uri) throws Exception {

        //Build DOM
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(String.valueOf(uri));

        //Create XPath
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();

        //Variable inicialization
        String level = "/*";
        ArrayList<ArrayList<ArrayList<String>>> xmlelements = new ArrayList<ArrayList<ArrayList<String>>>();
        Integer lc = 1; //Level counter
        Object[] solution = new Object[2];
        solution[0] = xmlelements;
        solution[1] = lc;

        solution = new XMLReadElement().XMLReadElement(doc, xpath, level, solution);
        xmlelements = (ArrayList<ArrayList<ArrayList<String>>>) solution[0];
        return xmlelements;
    }
}
