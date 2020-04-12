package es.ehu.domain.manufacturing.test;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class XPathAppTest {
    public static void main(String[] args) throws Exception {
        //Build DOM

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
//        Document doc = builder.parse("classes/es/ehu/domain/manufacturing/test/inventory.xml");
        Document doc = builder.parse("classes/resources/AppInstances/MP1.xml");

        //Create XPath

        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();

        System.out.println("\n1) Get the first level element");

        // 1) Get the first level element
        String first_level="/*";
        XPathExpression expr = xpath.compile("name("+first_level+")");
        Object result1 = expr.evaluate(doc, XPathConstants.STRING);
        String first = (String) result1;
        System.out.println(first);

        System.out.println("\n2) Get attribute list and value of the first level element");

        // 2) Get the attributes of the first level element
        expr = xpath.compile(first_level+"/@*");
        Object result2 = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result2;
        for (int i = 0; i < nodes.getLength(); i++) {
            String name = nodes.item(i).getNodeName();
            String value = nodes.item(i).getNodeValue();
            System.out.println(name+" = "+value);
        }
    }
}
