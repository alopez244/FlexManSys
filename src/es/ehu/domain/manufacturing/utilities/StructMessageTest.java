package es.ehu.domain.manufacturing.utilities;

import java.util.HashMap;

public class StructMessageTest {

    private String messageGson;
    private String action;
    private Boolean newData = false;
    private Integer performative;
    private HashMap<String,HashMap<String,HashMap<String,String>>> testResults;
    private HashMap<String,HashMap<String,HashMap<String,String>>> testResultsApp;
    private HashMap<String,HashMap<String,HashMap<String,String>>> testResultsErr;
    private HashMap<String,HashMap<String,HashMap<String,String>>> testResultsNeg;
    private HashMap<String,HashMap<String,HashMap<String,String>>> testResultsMachine;


    public StructMessageTest() {
    }

    public void setTestResults(HashMap _results) {
        this.testResults = _results;
    }

    public void setTestResultsApp(HashMap _resultsApp) {
        this.testResultsApp = _resultsApp;
    }

    public void setTestResultsErr(HashMap _resultsErr) { this.testResultsErr = _resultsErr; }

    public void setTestResultsNeg(HashMap _resultsNeg) { this.testResultsNeg = _resultsNeg; }

    public void setTestResultsMachine(HashMap _resultsMachine) { this.testResultsMachine = _resultsMachine; }


    public HashMap readTestResults() {
        return this.testResults;
    }

    public HashMap readTestResultsErr() {
        return this.testResultsErr;
    }

    public HashMap readTestResultsNeg() {
        return this.testResultsNeg;
    }

    public HashMap readTestResultsApp() {
        return this.testResultsApp;
    }

    public HashMap readTestResultsMachine() { return this.testResultsMachine; }





    public void setAction(String _action) {
        this.action = _action;
    }

    public void setMessage(String _message) {
        this.messageGson = _message;
    }

    public void setPerformative(int _performative) {
        this.performative = _performative;
    }

    public void setNewData(Boolean boolIn) {
        this.newData = boolIn;
    }

    public String readAction() {
        return this.action;
    }

    public String readMessage() {
        return this.messageGson;
    }

    public boolean readNewData() {
        return this.newData;
    }

    public int readPerformative() {
        return this.performative;
    }



}