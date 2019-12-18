package es.ehu.platform.utilities;

import java.util.Hashtable;

public class Cmd {
  
  public Hashtable<String, String> attribs = null;
  public String cmd= "";
  public String who= "";
  
  public Cmd(String cmd){
    
    String[] cmds = cmd.split(" ");
    if (cmds.length>0) this.cmd = cmds[0];
    if (cmds.length>1) this.who = cmds[1];
    if (cmds.length>3) this.attribs  = processAttribs(cmds);
  }

  
  public Hashtable<String, String> processAttribs(String... cmdLine){
    
    
    if (cmdLine.length < 3) return null; //no hay atributos

    Hashtable<String, String> attribs = new Hashtable<String, String>();
    String attrib = "attrib";

    for (int i = 2; i < cmdLine.length; i++) {
      if (cmdLine[i].contains("=")) { // encuentro otro atributo
        String[] attribDef = cmdLine[i].split("=");
        attrib = attribDef[0];

        attribs.put(attrib, (attribDef.length>1)?attribDef[1]:""); // puede estar vacío
      } else
        attribs.put(attrib, attribs.get(attrib)+" "+cmdLine[i]);
     String attribValue = attribs.get(attrib);
     while (attribValue.contains("{")) attribValue = attribValue.replace("{", "(");
     while (attribValue.contains("}")) attribValue = attribValue.replace("}", ")");
     while (attribValue.contains("#")) attribValue = attribValue.replace("#", "=");
     attribs.put(attrib, attribValue); 
     
    }
    return attribs;
  }
  
}
