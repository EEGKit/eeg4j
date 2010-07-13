package it.hakvoort.nia;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class NiaSample {
	
	// the number of this sample
	public int number = -1;
	
	// the value of this sample
	public int value = -1;
	
	public NiaSample(int number, int value) {
		this.number = number;
		this.value = value;
	}
	
	public String toString() {
		return String.format("%s: value: %s", number, value);
	}
}