package es.ehu.platform.utilities;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;

public class XMLReadElement {
    public static Object[] XMLReadElement (Document doc, XPath xpath, String level,
                                           Object[] solution) throws XPathExpressionException {

        //Obtain elements from the solution object
        ArrayList<ArrayList<ArrayList<String>>> xmlelements = new ArrayList<ArrayList<ArrayList<String>>>();
        xmlelements = (ArrayList<ArrayList<ArrayList<String>>>) solution[0];
        Integer lc = (Integer) solution[1];

        //Start for the first level
        //The nodeset of elements at this level is obtained
        XPathExpression expr = xpath.compile(level);
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;

        //Reading Loop
        //Nested top-down element reading
        for (int i = 0; i <nodes.getLength(); i++) {

            //A new position is added to the main (level 1) ArrayList
            //This level has one position per element read from the XML
            int size = xmlelements.size();
            xmlelements.add(size, new ArrayList<ArrayList<String>>());

            //The level 2 ArrayList is initialized with three positions
            //The element type is added to the first position of the level 2 ArrayList
            xmlelements.get(size).add(0, new ArrayList<String>());
            xmlelements.get(size).get(0).add(0,nodes.item(i).getNodeName());

            //The second position stores the level of the element
            xmlelements.get(size).add(1, new ArrayList<String>());
            xmlelements.get(size).get(1).add(0,lc.toString());

            //The third and fourth positions will host The attribute names and values, respectively
            xmlelements.get(size).add(2, new ArrayList<String>());
            xmlelements.get(size).add(3, new ArrayList<String>());

            //The attributes of this node are obtained and registered in their respective positions
            NamedNodeMap node_att = nodes.item(i).getAttributes();
            for (int j = 0; j <node_att.getLength(); j++) {
                Node attrib = node_att.item(j);

                //Filtering unnecesary attributes from the first level
                String condition = (String) attrib.getNodeName();
                if(condition.startsWith("xmlns")||condition.startsWith("xsi")){

                }else {
                    xmlelements.get(size).get(2).add(j,attrib.getNodeName());
                    xmlelements.get(size).get(3).add(j,attrib.getNodeValue());
                }
            }

            //The element is registered in the return object
            solution[0] = xmlelements;

            //Now search for elements in the next level of the XML file
            if (nodes.item(i).hasChildNodes()){

                //The level counter is increased by one, as the loop goes one level down
                lc = lc + 1;
                solution[1] = lc;

                //Search for elements on the first element of the next level of the XML file
                int i_1=i+1;
                String new_level=level+"["+i_1+"]/*";

                //Invoke this method recursively
                solution = new XMLReadElement().XMLReadElement(doc, xpath, new_level, solution);
                lc = (Integer) solution[1];
                System.out.println();
            } else {

                //If this element has no children, it is the last of its branch. The for loop must conclude
                if(nodes.getLength()-i==1){
                    lc = lc - 1; //There are no more items at tbis level, the loop comes back to the upper level
                    solution[1] = lc;
                    break; //If this condition is met, this is the last iteration of the loop
                }
            }
        }
        return solution;
    }
}
