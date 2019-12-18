package es.ehu.domain.eHealth.functionality;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class PatientProperties {
private String sFileName = ""; 
	
	public PatientProperties(String fileName) {
		this.sFileName = fileName;		
	}
	
	public HashMap<Object, Object> getProperties() {
		HashMap<Object, Object> propHashMap = null;
		
		try {
		InputStream inputStream = new FileInputStream(new File(this.sFileName));
		Properties tempProp = new Properties();
		tempProp.load(inputStream);
		inputStream.close();
		propHashMap = new HashMap<Object, Object>(tempProp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return propHashMap;
	}
}
