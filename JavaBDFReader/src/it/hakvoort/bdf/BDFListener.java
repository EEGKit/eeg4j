package it.hakvoort.bdf;

import it.hakvoort.bdf.BDFDataRecord;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public interface BDFListener {

	public void receivedRecord(BDFDataRecord record);
	
}
