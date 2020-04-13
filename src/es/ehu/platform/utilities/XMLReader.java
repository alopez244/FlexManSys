package es.ehu.platform.utilities;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.concurrent.ConcurrentHashMap;

public class XMLReader {

    public static void readFile (String uri) throws Exception {

        //Build DOM

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(String.valueOf(uri));

        //Create XPath

        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();

        //Variable inicialization

        String elementType = "system";
        String parentId = "system";
        String level = "/*";
        Integer z = 1; //Level counter
        ConcurrentHashMap<String, String> attributes = new ConcurrentHashMap<String, String>();
        ConcurrentHashMap<String, ConcurrentHashMap<String, String>> parent_att = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>();
        ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, String>>> xml_element = new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, String>>>();

        //Just in case, the HasMaps are cleared
        attributes.clear();
        parent_att.clear();
        xml_element.clear();

        //Reading Loop
        while (elementType != ""){

            //Start for the first level
            //The nodeset of elements at this level is obtained
            XPathExpression expr = xpath.compile(level);
            Object result = expr.evaluate(doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;

            //The number of nodes is obtained
//            Integer number = nodes.getLength();
//            System.out.println("Number of elements at this level: "+number);

            //Nested top-down element reading
            for (int i = 0; i <nodes.getLength(); i++) {

                //The attributes of this node are obtained
                NamedNodeMap node_att = nodes.item(i).getAttributes();
                for (int j = 0; j <node_att.getLength(); j++) {
                    Node attrib = node_att.item(j);
                    attributes.put(attrib.getNodeName(),attrib.getNodeValue());
                }

                //The parentId for the first level is system
                parent_att.put(parentId,attributes);

                //The element Hashmap is completed with the element type
                xml_element.put(nodes.item(i).getNodeName(),parent_att);

                //Now search for elements in the second level
                if (nodes.item(i).hasChildNodes()){

                    //Search for elements on the second level
                    level=level+"/*";
                    XPathExpression expr2 = xpath.compile(level);
                    Object result2 = expr2.evaluate(doc, XPathConstants.NODESET);
                    NodeList nodes2 = (NodeList) result2;
                    for (int k = 0; k <nodes2.getLength(); k++) {

                        //The attributes of this node are obtained
                        node_att = nodes2.item(k).getAttributes();
                        for (int l = 0; l <node_att.getLength(); l++) {
                            Node attrib = node_att.item(l);
                            attributes.put(attrib.getNodeName(),attrib.getNodeValue());
                        }

                        //The parentId for the second level is the first level element type
                        //xml_element.
                        parent_att.put("manufacturingPlan",attributes);

                        //The element Hashmap is completed with the element type
                        xml_element.put(nodes2.item(k).getNodeName(),parent_att);

                        //Now search for elements in the second level
                        if (nodes.item(k).hasChildNodes()){

                            //Search for elements on the second level
//                            level=level+"/*";
//                            XPathExpression expr2 = xpath.compile(level);
//                            Object result2 = expr2.evaluate(doc, XPathConstants.NODESET);
//                            NodeList nodes2 = (NodeList) result2;

                        } else {
                            break; //If these element has no children continue with its next sibling
                        }
                    }

                } else {
                    break; //If these element has no children continue with its next sibling
                }
            }

//            //The element type of the elements of this level is obtained (name of the XPath node)
//            expr = xpath.compile("name("+level+")");
//            Object result3 = expr.evaluate(doc, XPathConstants.STRING);
//            elementType = (String) result3;
//            if (elementType == ""){
//                break;
//            }


        }


//        while (element != ""){
//
//            //The parent is equal to the previous level element
//            parent = element;
//
//            //The name of the element is obtained (name of the XPath node)
//            XPathExpression expr = xpath.compile("name("+level+")");
//            Object result1 = expr.evaluate(doc, XPathConstants.STRING);
//            element = (String) result1;
//            if (element == ""){
//                break;
//            }
//            System.out.println("Element: "+element);
//            System.out.println("Parent: "+parent);
//
//            //Count the siblings of the node
//            expr = xpath.compile("count("+level+")");
//            Object result3 = expr.evaluate(doc, XPathConstants.NUMBER);
//            Double number = (Double) result3;
//            Integer num = number.intValue();
//            System.out.println("Number of elements at this level: "+num);
//
//            //The attributes of the element are obtained (attributes names and values)
//            expr = xpath.compile(level+"/@*");
//            Object result2 = expr.evaluate(doc, XPathConstants.NODESET);
//            NodeList nodes = (NodeList) result2;
//            System.out.println("Element attributes:\n");
//            for (int i = 0; i < nodes.getLength(); i++) {
//                String name = nodes.item(i).getNodeName();
//                String value = nodes.item(i).getNodeValue();
//                System.out.println(name+" = "+value);
//            }
//            System.out.println();
//
//            //Update the level string
//            level=level+"/*";
//        }
    }
}
