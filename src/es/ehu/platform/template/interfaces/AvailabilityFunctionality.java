package es.ehu.platform.template.interfaces;

import java.io.Serializable;
import java.util.ArrayList;

import jade.core.AID;

public interface AvailabilityFunctionality extends Serializable{

  public String getState ();
  
  public void setState (String state);

}
