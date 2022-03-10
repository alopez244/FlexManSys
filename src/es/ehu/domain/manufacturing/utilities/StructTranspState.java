package es.ehu.domain.manufacturing.utilities;

public class StructTranspState {

    private int battery;
    private String currentPos;
    private boolean assetLiveness;

    public StructTranspState() {}

    public void setBattery(int _entero) { this.battery = _entero; }
    public void setCurrentPos(String _cadena) { this.currentPos = _cadena; }
    public void setAssetLiveness(boolean _assetLiveness) { this.assetLiveness = _assetLiveness; }

    public int getBattery() { return this.battery; }
    public String getCurrentPos() { return this.currentPos; }
    public boolean getassetLiveness() { return this.assetLiveness; }

}
