package es.ehu.platform.utilities;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class XMLWriter {

    public static void writeFile (ArrayList<ArrayList<ArrayList<String>>> xmlElements, String planID){

        //Inicialización de variables
        Integer level = 0; //Nivel del elemento que se añade al doc
        Integer attrSize = 0; //Número de atributos que tiene el elemento
        HashMap parents = new HashMap<Integer, Element>(); //HashMap que indica el elemento padre actual para cada nivel
        Element root; //El elemento raíz
        Element child; //El elemento que se va a añadir recursivamente

        //Se solicita el nombre del fichero que se quiere crear
        String appPath="src/resources/Results/";
        String file="MP";
        file = file.concat(planID);
        String xmlFilePath=appPath+file+".xml";

        //Creo el fichero de texto (si no existe)
        try {
            File myObj = new File(xmlFilePath);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists. It will be overwritten.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        //Ahora creo el objeto DOM
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            System.out.println("Document can not be generated");
        }
        Document doc = builder.newDocument();

        //Recorro la estructura de arrays
        for (int i = 0; i < xmlElements.size(); i++){

            //Primero se comprueba a qué nivel está el elemento actual
            level = Integer.valueOf(xmlElements.get(i).get(1).get(0));
            if (level == 1){

                //Si es el elemento raíz (elemento de nivel 1) se lo añadimos al documento
                root = doc.createElement(xmlElements.get(i).get(0).get(0));
                doc.appendChild(root);

                //Se comprueba si el elemento raíz tiene atributos
                attrSize = xmlElements.get(i).get(2).size();
                if (attrSize > 0){ //Si tiene atributos, se le añaden
                    for (int j = 0; j < attrSize; j++){
                        root.setAttribute(xmlElements.get(i).get(2).get(j),xmlElements.get(i).get(3).get(j));
                    }
                }

                //El elemento raíz será el primer elemento padre
                parents.put(level,root);
            } else {

                //En el resto de elementos, primero se comprueba que en el HashMap parents está el elemento al que hay que añadirle este nuevo hijo
                //El padre será el elemento de un nivel menos
                if (parents.containsKey(level-1)){

                    //Si se sabe quién es el padre, añadimos el elemento
                    root= (Element) parents.get(level-1);
                    child = doc.createElement(xmlElements.get(i).get(0).get(0));
                    root.appendChild(child);

                    //Se comprueba si tiene atributos
                    attrSize = xmlElements.get(i).get(2).size();
                    if (attrSize > 0){ //Si tiene atributos, se le añaden
                        for (int k = 0; k < attrSize; k++){
                            child.setAttribute(xmlElements.get(i).get(2).get(k),xmlElements.get(i).get(3).get(k));
                        }
                    }

                    //Si ya había un elemento padre de este nivel en el HashMap se sustituye. Si no, se crea uno nuevo
                    if(parents.containsKey(level)){
                        parents.replace(level,child);
                    } else {
                        parents.put(level,child);
                    }
                }
            }
        }

        //Ahora se crea el fichero XML
        //Para ello, se transforma el objeto DOM en un fichero XML
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource domSource = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(new File(xmlFilePath));
            transformer.transform(domSource, streamResult);

            System.out.println("Done creating XML File");
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }
}
