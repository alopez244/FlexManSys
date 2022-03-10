package es.ehu.domain.manufacturing.utilities;

/* Este codigo define una clase definida como StructState, es decir, la estructura
   de datos que se va a emplear para recoger los datos obtenidos al suscribirse al
   topico de /flexmansys/state/leonardo
 */

public class StructTransportUnitState {

    // Contenido para pasarle al agente

    // private String[] content;

    // Campos de KobukiGeneral

    private String transport_unit_name;
    private String transport_unit_state;
    private Float battery;

    // Campos de KobukiObstacle

    private boolean detected_obstacle_bumper;
    private boolean detected_obstacle_camera;

    // Campos de KobukiPosition

    private boolean transport_in_dock;
    private String recovery_point;
    private Double odom_x;
    private Double odom_y;
    private Double rotation;

    // Campos de OdroidDate

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int seconds;

    public StructTransportUnitState() {}

    ///////////////////////////////

    public void setTransport_unit_name (String transport_unit_name) {
        this.transport_unit_name = transport_unit_name;
    }

    public void setTransport_unit_state (String transport_unit_state) {
        this.transport_unit_state = transport_unit_state;
    }

    public void setBattery (Float battery) {
        this.battery = battery;
    }

    ///////////////////////////////

    public void setDetected_obstacle_bumper (boolean detected_obstacle_bumper) {
        this.detected_obstacle_bumper = detected_obstacle_bumper;
    }

    public void setDetected_obstacle_camera (boolean detected_obstacle_camera) {
        this.detected_obstacle_camera = detected_obstacle_camera;
    }

    /////////////////////////////

    public void setTransport_in_dock (boolean transport_in_dock) {
        this.transport_in_dock = transport_in_dock;
    }

    public void setRecovery_point (String recovery_point) {
        this.recovery_point = recovery_point;
    }

    public void setOdom_x (Double odom_x) {
        this.odom_x = odom_y;
    }

    public void setOdom_y (Double odom_y) {
        this.odom_y = odom_y;
    }

    public void setRotation (Double rotation) {
        this.rotation = rotation;
    }


    ////////////////////////////

    public void setYear (int year) {
        this.year = year;
    }

    public void setMonth (int month) {
        this.month = month;
    }

    public void setDay (int day) {
        this.day = day;
    }

    public void setHour (int hour) {
        this.hour = hour;
    }

    public void setMinute (int minute) {
        this.minute = minute;
    }

    public void setSeconds (int seconds) {
        this.seconds = seconds;
    }

    ///////////////////////////

    public String getTransport_unit_name() { return this.transport_unit_name; }
    public String getTransport_unit_state() { return this.transport_unit_state; }
    public Float getBattery() { return this.battery; }

    public boolean getDetected_obstacle_bumper() { return this.detected_obstacle_bumper; }
    public boolean getDetected_obstacle_camera() { return this.detected_obstacle_camera; }

    public boolean getTransport_in_dock() { return this.transport_in_dock; }
    public String getRecovery_point() { return this.recovery_point; }
    public Double getOdom_x() { return this.odom_x; }
    public Double getOdom_y() { return this.odom_y; }
    public Double getRotation() { return this.rotation; }

    public int getYear() { return this.year; }
    public int getMonth() { return this.month; }
    public int getDay() { return this.day; }
    public int getHour() { return this.hour; }
    public int getMinute() { return this.minute; }
    public int getSeconds() { return this.seconds; }

    // public String[] getContent() { return this.content; }

}
