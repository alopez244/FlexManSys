package es.ehu.domain.manufacturing.utilities;

import java.util.ArrayList;

public class StructMplanAgentState {
    private ArrayList<ArrayList<ArrayList<ArrayList<String>>>> ordersTraceability;
    private ArrayList<String> sonAgentID;
    private boolean firstTime;
    private String parenAgentID;
    private ArrayList<String> replicas;
    private boolean newOrder;
    private Integer orderIndex;

    public StructMplanAgentState() {}

    public void setordersTraceability(ArrayList<ArrayList<ArrayList<ArrayList<String>>>> _traceability) { this.ordersTraceability = _traceability; }
    public void setsonAgentID(ArrayList<String> _sonAgentID) { this.sonAgentID = _sonAgentID; }
    public void setfirstTime (boolean _firstTime) { this.firstTime = _firstTime; }
    public void setparenAgentID (String _parenAgentID) { this.parenAgentID = _parenAgentID; }
    public void setreplicas (ArrayList<String> _replicas) { this.replicas = _replicas; }
    public void setnewOrder (boolean _newOrder) { this.newOrder = _newOrder; }
    public void setorderIndex (Integer _orderIndex) { this.orderIndex = _orderIndex; }

    public ArrayList<ArrayList<ArrayList<ArrayList<String>>>> getordersTraceability() { return this.ordersTraceability; }
    public ArrayList<String> getsonAgentID() { return this.sonAgentID; }
    public boolean getfirstTime() { return this.firstTime; }
    public String getparenAgentID() { return this.parenAgentID; }
    public ArrayList<String> getreplicas() { return this.replicas; }
    public boolean getnewOrder() { return this.newOrder; }
    public Integer getorderIndex() { return this.orderIndex; }

}
