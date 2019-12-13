package es.ehu.domain.sputnik;
import java.io.*;
import java.util.Date;

public class Dato implements Serializable {

private long millis;
private int valor;

 public Dato(int valor) {
	  this.millis = System.currentTimeMillis();
	  this.valor = valor;
  }

  public String toString() {
    return("valor="+valor);
  }
  
  public long getCreationMillis (){
	  return this.millis;
  }
  public void incrementa (){
	  this.valor++;
  }
  public void incrementa (int valor){
	  this.valor += valor;
  }
  
  public void decrementa (int cuanto){
	  this.valor -= cuanto;
  }
  
  public int getValor(){
	  return this.valor;
  }
  
  

}
