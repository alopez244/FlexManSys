package com.github.nombre_paquete.nombre_proyecto;

import com.github.nombre_paquete.nombre_proyecto.movini;
import jade.core.Agent;
import jade.core.AID;

public class agente_movini_mod extends Agent {

  public agente_movini_mod() {
  }

  protected void setup() {
    movini prueba = new movini(this, 5);
  }

  protected void takeDown() {
  }
}