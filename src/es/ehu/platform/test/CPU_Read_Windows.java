package es.ehu.platform.test;

import org.apache.commons.lang.SystemUtils;

import java.io.*;

public class CPU_Read_Windows {
    public static void main(String[] args) {
        Boolean test= SystemUtils.IS_OS_WINDOWS;
        System.out.println(test);
        String result=null;
        try {
            Process proc = Runtime.getRuntime().exec("wmic cpu get LoadPercentage");
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String s = null;
            while((s = stdInput.readLine()) != null) {
                if (!s.equals("")){
                    String s_trim=s.trim();
                    try {
                        int i =Integer.parseInt(s_trim);
                        result=s;
                    } catch (NumberFormatException nfe) {
//                                System.out.println(s + " is not a number");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println(result);
    }
}
