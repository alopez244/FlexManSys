package es.ehu.platform.test;

import es.ehu.platform.utilities.XMLReader;

public class main {
    public static void main(String[] args) throws Exception {
        String uri="classes/resources/AppInstances/MP1.xml";
        XMLReader fileReader = new XMLReader();
        fileReader.readFile(uri);
    }
}
