/*
 Part of Libnodave, a free communication library for Siemens S7 300/400 via
 the MPI adapter 6ES7 972-0CA22-0XAC
 or  MPI adapter 6ES7 972-0CA33-0XAC
 or  MPI adapter 6ES7 972-0CA11-0XAC.
 */
package es.ehu.domain.manufacturing.libConcentrador.driver;

/**
 * @author Thomas Hergenhahn
 * 
 */
public class ResultSet
{
	private int errorState, numResults;
	public Result[] results;

	public void setErrorState(int error)
	{
		errorState = error;
	}

	public int getErrorState()
	{
		return errorState;
	};

	public void setNumResults(int nr)
	{
		numResults = nr;
	}

	public int getNumResults()
	{
		return numResults;
	};
}
