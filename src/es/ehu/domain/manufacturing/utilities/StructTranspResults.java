package es.ehu.domain.manufacturing.utilities;

public class StructTranspResults {

    private int battery;
    private String currentPos;
    private float initial_timeStamp;
    private float final_timeStamp;

    public StructTranspResults() {}

    public void setBattery(int _entero) { this.battery = _entero; }
    public void setCurrentPos(String _cadena) { this.currentPos = _cadena; }
    public void setInitial_timeStamp (float _initial_timeStamp) { this.initial_timeStamp = _initial_timeStamp; }
    public void setFinal_timeStamp (float _final_timeStamp) { this.final_timeStamp = _final_timeStamp; }


    public int getBattery() { return this.battery; }
    public String getCurrentPos() { return this.currentPos; }
    public float getInitial_timeStamp() { return this.initial_timeStamp; }
    public float getFinal_timeStamp() { return final_timeStamp; }
}
