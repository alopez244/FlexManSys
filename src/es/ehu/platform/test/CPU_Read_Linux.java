package es.ehu.platform.test;

import java.io.*;

public class CPU_Read_Linux {
    public static void main(String[] args) {
        try {
            Process proc = Runtime.getRuntime().exec("sar -p 1 2");
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String s = null;
            while((s = stdInput.readLine()) != null) {
                if(s.contains("Media")) {
                    String[] parts = s.split(" ");
                    System.out.println(parts[parts.length - 1]);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
