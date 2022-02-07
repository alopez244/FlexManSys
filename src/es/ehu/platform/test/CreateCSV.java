package es.ehu.platform.test;

import es.ehu.platform.utilities.TimestampsToCSV;

public class CreateCSV {
    public static void main(String[] args) {
        TimestampsToCSV.getInstance().FillCSV();
    }
}
