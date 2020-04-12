package es.ehu.domain.manufacturing.test;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class XPathExample {
    public static void main(String[] args) throws Exception {
        //Build DOM

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse("classes/es/ehu/domain/manufacturing/test/inventory.xml");

        //Create XPath

        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();

        System.out.println("n//1) Get book titles written after 2001");

        // 1) Get book titles written after 2001
        XPathExpression expr = xpath.compile("//book[@year>2001]/title/text()");
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); i++) {
            System.out.println(nodes.item(i).getNodeValue());
        }

        System.out.println("n//2) Get book titles written before 2001");

        // 2) Get book titles written before 2001
        expr = xpath.compile("//book[@year<2001]/title/text()");
        result = expr.evaluate(doc, XPathConstants.NODESET);
        nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); i++) {
            System.out.println(nodes.item(i).getNodeValue());
        }

        System.out.println("n//3) Get book titles cheaper than 8 dollars");

        // 3) Get book titles cheaper than 8 dollars
        expr = xpath.compile("//book[price<8]/title/text()");
        result = expr.evaluate(doc, XPathConstants.NODESET);
        nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); i++) {
            System.out.println(nodes.item(i).getNodeValue());
        }

        System.out.println("n//4) Get book titles costlier than 8 dollars");

        // 4) Get book titles costlier than 8 dollars
        expr = xpath.compile("//book[price>8]/title/text()");
        result = expr.evaluate(doc, XPathConstants.NODESET);
        nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); i++) {
            System.out.println(nodes.item(i).getNodeValue());
        }

        System.out.println("n//5) Get book titles added in first node");

        // 5) Get book titles added in first node
        expr = xpath.compile("//book[1]/title/text()");
        result = expr.evaluate(doc, XPathConstants.NODESET);
        nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); i++) {
            System.out.println(nodes.item(i).getNodeValue());
        }

        System.out.println("n//6) Get book title added in last node");

        // 6) Get book title added in last node
        expr = xpath.compile("//book[last()]/title/text()");
        result = expr.evaluate(doc, XPathConstants.NODESET);
        nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); i++) {
            System.out.println(nodes.item(i).getNodeValue());
        }

        System.out.println("n//7) Get all writers");

        // 7) Get all writers
        expr = xpath.compile("//book/author/text()");
        result = expr.evaluate(doc, XPathConstants.NODESET);
        nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); i++) {
            System.out.println(nodes.item(i).getNodeValue());
        }

        System.out.println("n//8) Count all books titles ");

        // 8) Count all books titles
        expr = xpath.compile("count(//book/title)");
        result = expr.evaluate(doc, XPathConstants.NUMBER);
        Double count = (Double) result;
        System.out.println(count.intValue());

        System.out.println("n//9) Get book titles with writer name start with Neal");

        // 9) Get book titles with writer name start with Neal
        expr = xpath.compile("//book[starts-with(author,'Neal')]");
        result = expr.evaluate(doc, XPathConstants.NODESET);
        nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); i++) {
            System.out.println(nodes.item(i)
                    .getChildNodes()
                    .item(1)                //node <title> is on first index
                    .getTextContent());
        }

        System.out.println("n//10) Get book titles with writer name containing Niven");

        // 10) Get book titles with writer name containing Niven
        expr = xpath.compile("//book[contains(author,'Niven')]");
        result = expr.evaluate(doc, XPathConstants.NODESET);
        nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); i++) {
            System.out.println(nodes.item(i)
                    .getChildNodes()
                    .item(1)                //node <title> is on first index
                    .getTextContent());
        }

        System.out.println("//11) Get book titles written by Neal Stephenson");

        // 11) Get book titles written by Neal Stephenson
        expr = xpath.compile("//book[author='Neal Stephenson']/title/text()");
        result = expr.evaluate(doc, XPathConstants.NODESET);
        nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); i++) {
            System.out.println(nodes.item(i).getNodeValue());
        }

        System.out.println("n//12) Get count of book titles written by Neal Stephenson");

        // 12) Get count of book titles written by Neal Stephenson
        expr = xpath.compile("count(//book[author='Neal Stephenson'])");
        result = expr.evaluate(doc, XPathConstants.NUMBER);
        count = (Double) result;
        System.out.println(count.intValue());

        System.out.println("n//13) Reading comment node ");

        // 13) Reading comment node
        expr = xpath.compile("//inventory/comment()");
        result = expr.evaluate(doc, XPathConstants.STRING);
        String comment = (String) result;
        System.out.println(comment);
    }
}