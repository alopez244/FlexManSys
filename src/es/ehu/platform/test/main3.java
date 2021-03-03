package es.ehu.platform.test;

import es.ehu.domain.manufacturing.utilities.MPlanInterpreter;
import es.ehu.platform.utilities.XMLReader;

import java.util.ArrayList;
import java.util.Scanner;

public class main3 {
    public static void main(String[] args) throws Exception {
        //String appPath="classes/resources/ManufacturingPlan Examples/";
        String appPath="classes/resources/AppInstances/";
        String file="";
        Scanner in = new Scanner(System.in);
        System.out.println("Please, introduce the name of the XML File you want to register.");
        System.out.print("File: ");
        file = in.nextLine();
        System.out.println();
        String uri=appPath+file;
        XMLReader fileReader = new XMLReader();
        ArrayList<ArrayList<ArrayList<String>>> xmlelements = fileReader.readFile(uri);
        System.out.println();
        //MPlanInterpreter.getManEntities(null, xmlelements);
    }
}
