package it.hakvoort.eeg.marker;


/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public interface MarkerListener {

	void receivedMarker(byte marker);
	
}
