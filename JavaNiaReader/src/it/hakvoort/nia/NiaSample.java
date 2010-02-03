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
	
	// the hitcount 
	public int hitcount = -1;
	
	// the miscount
	public int miscount = -1;
	
	// the time the read took
	public long time = -1;
	
	public NiaSample(int number, int miscount, int hitcount, int value, long time) {
		this.number = number;
		this.value = value;
		
		this.hitcount = hitcount;
		this.miscount = miscount;
		
		this.time = time;
	}
	
	public String toString() {
		return String.format("%s: value: %s, mis:%s, hit:%s", number, value, miscount, hitcount);
	}
}