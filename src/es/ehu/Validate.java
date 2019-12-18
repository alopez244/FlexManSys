package es.ehu;


import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

import java.net.URL;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
//import java.io.File; // if you use File
import java.io.IOException;
import java.io.InputStream;


public class Validate {

  public static void main(String[] args) {
    // 
    Validate v = new Validate();
    System.out.println(v.getClass().getResource("/").getPath());
    //URL schemaFile = new URL("http://host:port/filename.xsd");
 // webapp example xsd: 
 // URL schemaFile = new URL("http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd");
 // local file example:

 File schemaFile = new File("c:/temp/xsd.xsd");//Concepts.xsd"); // etc.
 Source xmlFile = new StreamSource(new File("c:/temp/xml.xml"));///Concept.xml"));
 
// File schemaFile = new File("bin/xsd.xsd");//Concepts.xsd"); // etc.
// Source xmlFile = new StreamSource(new File("bin/xml.xml"));///Concept.xml"));
 
 SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
 
 //SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
 
 //schemaFactory.setResourceResolver(new ClasspathResourceResolver());
 
 try {
   
   
   
   
   
   //validator.validate(new StreamSource(new File("./foo.xml")));
   
   
   
   Schema schema = schemaFactory.newSchema(schemaFile);
   Validator validator = schema.newValidator();
   validator.validate(xmlFile);
   System.out.println(xmlFile.getSystemId() + " is valid");
 } catch (SAXException e) {
   System.out.println(xmlFile.getSystemId() + " is NOT valid reason:" + e);
 } catch (IOException e) {
   e.printStackTrace();
 }


  }
  

  
  

}
