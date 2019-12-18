package es.ehu.platform.template.interfaces;

import java.io.Serializable;

import jade.core.AID;

public interface AvailabilityFunctionality extends Serializable{

  public Object getState ();
  
  public void setState (Object state);
}
