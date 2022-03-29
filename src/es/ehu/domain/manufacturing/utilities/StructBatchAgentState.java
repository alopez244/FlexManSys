package es.ehu.domain.manufacturing.utilities;

import java.util.ArrayList;
import java.util.*;

public class StructBatchAgentState {
    private ArrayList<ArrayList<ArrayList<ArrayList<String>>>> productsTraceability;
    private ArrayList<String> actionList;
    private boolean firstTime;
    private String parenAgentID;
    private ArrayList<String> replicas;
    private String FT;
    private int itemNbr;
    private Date DateOfDelayAsk;
    private long delaynum;
    private Date ExpFinishDate;



    public StructBatchAgentState() {}

    public void setproductsTraceability(ArrayList<ArrayList<ArrayList<ArrayList<String>>>> _traceability) { this.productsTraceability = _traceability; }
    public void setactionList(ArrayList<String> _actionList) { this.actionList = _actionList; }
    public void setfirstTime (boolean _firstTime) { this.firstTime = _firstTime; }
    public void setparenAgentID (String _parenAgentID) { this.parenAgentID = _parenAgentID; }
    public void setreplicas (ArrayList<String> _replicas) { this.replicas = _replicas; }
    public void setFT (String _FT) { this.FT = _FT; }
    public void setitemNbr (int _itemNbr) { this.itemNbr = _itemNbr; }
    public void setDateOfDelayAsk (Date _DateOfDelayAsk) { this.DateOfDelayAsk = _DateOfDelayAsk; }
    public void setdelaynum (long _delaynum) { this.delaynum = _delaynum; }
    public void setExpFinishDate (Date _ExpFinishDate) { this.ExpFinishDate = _ExpFinishDate; }

    public ArrayList<ArrayList<ArrayList<ArrayList<String>>>> getproductsTraceability() { return this.productsTraceability; }
    public ArrayList<String> getactionList() { return this.actionList; }
    public boolean getfirstTime() { return this.firstTime; }
    public String getparenAgentID() { return this.parenAgentID; }
    public ArrayList<String> getreplicas() { return this.replicas; }
    public String getFT() { return this.FT; }
    public int getitemNbr() { return this.itemNbr; }
    public Date getDateOfDelayAsk() { return this.DateOfDelayAsk; }
    public long getdelaynum() { return this.delaynum; }
    public Date getExpFinishDate() { return this.ExpFinishDate; }

}
