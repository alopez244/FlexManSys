/**
 * This class stores in a repository the received data
 */
package es.ehu.domain.eHealth.functionality;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;

public class RepositoryStorage {
	private String repositoryFile = "";

	public RepositoryStorage(String repository) {
		repositoryFile = repository;

		// when a new repository is requested, a new .txt file is created with the date.
		Calendar c1 = Calendar.getInstance();
		String currentDate = "" + c1.get(Calendar.YEAR);
		String cadena = Integer.toString(c1.get(Calendar.MONTH));
		if (cadena.length() == 1)
			cadena = "0" + cadena;
		currentDate += cadena;
		cadena = Integer.toString(c1.get(Calendar.DATE));
		if (cadena.length() == 1)
			cadena = "0" + cadena;
		currentDate += cadena;
		File fFichero = null;
		int cont = 0;
		do {
			cont++;
			repositoryFile = repositoryFile + currentDate + "_" + cont + ".txt";
			fFichero = new File(repositoryFile);
		} while (fFichero.exists());
	}

	public void pulseStorage(int pulseValue, Date timeStamp, int patientID) {
		FileWriter fFichero = null;
		PrintWriter pw = null;
		try
		{
			fFichero = new FileWriter(repositoryFile, true);
			pw = new PrintWriter(fFichero);

			pw.println("---   " + timeStamp.toString() + "   ---");
			pw.println("PatientID: " + patientID + "\t\tPulse: " + pulseValue);
			pw.println("------------------------------------------------------");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != fFichero)
					fFichero.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
}
