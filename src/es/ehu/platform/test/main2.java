package es.ehu.platform.test;

import es.ehu.platform.test.XMLReader_V2;

import java.util.ArrayList;
import java.util.Scanner;

public class main2 {
    public static void main(String[] args) throws Exception {
        String appPath="classes/resources/AppInstances/";
        String file="";
        Scanner in = new Scanner(System.in);
        System.out.println("Please, introduce the name of the XML File you want to register.");
        System.out.print("File: ");
        file = in.nextLine();
        System.out.println();
        String uri=appPath+file;
        XMLReader_V2 fileReader = new XMLReader_V2();
        ArrayList<ArrayList<ArrayList<String>>> xmlelements = fileReader.readFile(uri);
        System.out.println();
    }
}
