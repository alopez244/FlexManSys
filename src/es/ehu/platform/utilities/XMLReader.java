package es.ehu.platform.utilities;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class XMLReader {

    public static void XMLReader (String uri) throws Exception {

        //Build DOM

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(String.valueOf(uri));

        //Create XPath

        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();

        //Reading Loop

        String element = "system";
        String parent = "system";
        String level = "/*";
        Integer level_counter = 1;

        while (element != ""){

            //The parent is equal to the previous level element
            parent = element;

            //The name of the element is obtained (name of the XPath node)
            XPathExpression expr = xpath.compile("name("+level+")");
            Object result1 = expr.evaluate(doc, XPathConstants.STRING);
            element = (String) result1;
            if (element == ""){
                break;
            }
            System.out.println("Element: "+element);
            System.out.println("Parent: "+parent);

            //The attributes of the element are obtained (attributes names and values)
            expr = xpath.compile(level+"/@*");
            Object result2 = expr.evaluate(doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result2;
            System.out.println("Element attributes:\n");
            for (int i = 0; i < nodes.getLength(); i++) {
                String name = nodes.item(i).getNodeName();
                String value = nodes.item(i).getNodeValue();
                System.out.println(name+" = "+value);
            }

            //Update the level counter
            level_counter=level_counter+1;
        }
    }
}
