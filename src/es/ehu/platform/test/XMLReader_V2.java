package es.ehu.platform.test;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import java.util.ArrayList;

public class XMLReader_V2 {

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
        Integer ec = 0; //Element counter
        ArrayList<ArrayList<ArrayList<String>>> xmlelements = new ArrayList<ArrayList<ArrayList<String>>>();

        //Reading Loop
        //Start for the first level
        //The nodeset of elements at this level is obtained
        XPathExpression expr = xpath.compile(level);
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;

        //Nested top-down element reading
        for (int i = 0; i <nodes.getLength(); i++) {

            //A new position is added to the main (level 1) ArrayList
            //This level has one position per element read from the XML
            xmlelements.add(ec, new ArrayList<ArrayList<String>>());

            //The level 2 ArrayList is initialized with three positions
            //The element type is added to the first position of the level 2 ArrayList
            xmlelements.get(ec).add(0, new ArrayList<String>());
            xmlelements.get(ec).get(0).add(0,nodes.item(i).getNodeName());

            //The second and third positions will host The attribute names and values, respectively
            xmlelements.get(ec).add(1, new ArrayList<String>());
            xmlelements.get(ec).add(2, new ArrayList<String>());

            //The attributes of this node are obtained and registered in their respective positions
            NamedNodeMap node_att = nodes.item(i).getAttributes();
            for (int j = 0; j <node_att.getLength(); j++) {
                Node attrib = node_att.item(j);
                xmlelements.get(ec).get(1).add(j,attrib.getNodeName());
                xmlelements.get(ec).get(2).add(j,attrib.getNodeValue());
            }

            //This element has been registered. The element counter is updated
            ec = ec + 1;

            //Now search for elements in the second level of the XML file
            if (nodes.item(i).hasChildNodes()){

                //Search for elements on the first element of the second level of the XML file
                int i_1=i+1;
                level="/*["+(i_1)+"]/*";
                XPathExpression expr2 = xpath.compile(level);
                Object result2 = expr2.evaluate(doc, XPathConstants.NODESET);
                NodeList nodes2 = (NodeList) result2;

                //Now we want to register only the nodes of the second level related to an specific father
                for (int k = 0; k <nodes2.getLength(); k++) {

                    //A new position is added to the main (level 1) ArrayList
                    //...
                    xmlelements.add(ec, new ArrayList<ArrayList<String>>());
                    xmlelements.get(ec).add(0, new ArrayList<String>());
                    xmlelements.get(ec).get(0).add(0,nodes2.item(i).getNodeName());
                    xmlelements.get(ec).add(1, new ArrayList<String>());
                    xmlelements.get(ec).add(2, new ArrayList<String>());

                    //The attributes of this node are obtained and registered in their respective positions
                    NamedNodeMap node_att2 = nodes2.item(k).getAttributes();
                    for (int l = 0; l <node_att2.getLength(); l++) {
                        Node attrib2 = node_att2.item(l);
                        xmlelements.get(ec).get(1).add(l,attrib2.getNodeName());
                        xmlelements.get(ec).get(2).add(l,attrib2.getNodeValue());
                    }

                    //This element has been registered. The element counter is updated
                    ec = ec + 1;

                    //Now search for elements in the third level
                    if (nodes2.item(k).hasChildNodes()){

                        //Search for elements on the third level of the XML file
                        int k_1=k+1;
                        level="/*["+i_1+"]/*["+k_1+"]/*";
                        XPathExpression expr3 = xpath.compile(level);
                        Object result3 = expr3.evaluate(doc, XPathConstants.NODESET);
                        NodeList nodes3 = (NodeList) result3;

                        //Now we want to register only the nodes of the third level related to an specific father
                        for (int m = 0; m <nodes3.getLength(); m++) {

                            //A new position is added to the main (level 1) ArrayList
                            //...
                            xmlelements.add(ec, new ArrayList<ArrayList<String>>());
                            xmlelements.get(ec).add(0, new ArrayList<String>());
                            xmlelements.get(ec).get(0).add(0, nodes3.item(i).getNodeName());
                            xmlelements.get(ec).add(1, new ArrayList<String>());
                            xmlelements.get(ec).add(2, new ArrayList<String>());

                            //The attributes of this node are obtained and registered in their respective positions
                            NamedNodeMap node_att3 = nodes3.item(m).getAttributes();
                            for (int n = 0; n < node_att3.getLength(); n++) {
                                Node attrib3 = node_att3.item(n);
                                xmlelements.get(ec).get(1).add(n, attrib3.getNodeName());
                                xmlelements.get(ec).get(2).add(n, attrib3.getNodeValue());
                            }

                            //This element has been registered. The element counter is updated
                            ec = ec + 1;
                        }
                    }else {
                        break; //If these element has no children continue with its next sibling
                    }
                }
            } else {
                break; //If these element has no children continue with its next sibling
            }
        }
        return xmlelements;
    }
}
