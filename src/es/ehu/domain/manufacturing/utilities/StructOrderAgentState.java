package es.ehu.domain.manufacturing.utilities;

import java.util.ArrayList;
import java.util.Date;

public class StructOrderAgentState {
    private ArrayList<ArrayList<ArrayList<ArrayList<String>>>> batchTraceability;
    private ArrayList<String> sonAgentID;
    private boolean firstTime;
    private String parenAgentID;
    private ArrayList<String> replicas;
    private ArrayList<ArrayList<String>> FT;
    private boolean newBatch;
    private Integer batchIndex;

    public StructOrderAgentState() {}

    public void setbatchTraceability(ArrayList<ArrayList<ArrayList<ArrayList<String>>>> _traceability) { this.batchTraceability = _traceability; }
    public void setsonAgentID(ArrayList<String> _sonAgentID) { this.sonAgentID = _sonAgentID; }
    public void setfirstTime (boolean _firstTime) { this.firstTime = _firstTime; }
    public void setparenAgentID (String _parenAgentID) { this.parenAgentID = _parenAgentID; }
    public void setreplicas (ArrayList<String> _replicas) { this.replicas = _replicas; }
    public void setFT (ArrayList<ArrayList<String>> _FT) { this.FT = _FT; }
    public void setnewBatch (boolean _newBatch) { this.newBatch = _newBatch; }
    public void setbatchIndex (Integer _batchIndex) { this.batchIndex = _batchIndex; }

    public ArrayList<ArrayList<ArrayList<ArrayList<String>>>> getbatchTraceability() { return this.batchTraceability; }
    public ArrayList<String> getsonAgentID() { return this.sonAgentID; }
    public boolean getfirstTime() { return this.firstTime; }
    public String getparenAgentID() { return this.parenAgentID; }
    public ArrayList<String> getreplicas() { return this.replicas; }
    public ArrayList<ArrayList<String>> getFT() { return this.FT; }
    public boolean getnewBatch() { return this.newBatch; }
    public Integer getbatchIndex() { return this.batchIndex; }

}
