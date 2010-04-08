package it.hakvoort.bdf;

/**
 *
 * The <code>BDFDataRecord</code> class represents a frame of a BDF stream.
 * It conains a record number, the number of channels and the samples for each of these channels.
 * 
 * @author	Gido Hakvoort (gido@hakvoort.it)
 * @see  	
 * 
 */
public class BDFDataRecord {
	
    /** number is used as an unique identifier for records. */
	public final int number;
	
    /** the number of channels in this record. */
	public final int channels;
	
	/** the recorded samples of the channels in this record. */
	public final int samples[];
	
    /**
     * Constructs a <code>BDFDataRecord</code> with only one channel.
     * The number of channels in this record will be 1.
     * 
	 * @param 	number
	 * 			The number of this record
	 * @param 	sample
	 * 			The recorded sample of the channel 
	 */
	public BDFDataRecord(int number, int sample) {
		this(number, new int[] {sample});
	}
	
	/**
     * Constructs a <code>BDFDataRecord</code> with multiple samples for multiple channel.
     * The number of channels in this record will be the <code>samples.lenght</code>.
     * 
	 * @param 	number
	 * 			The number of this record
	 * @param 	samples
	 * 			The recorded samples of the channels
	 */
	public BDFDataRecord(int number, int samples[]) {
		this.number 	= number;
		this.channels 	= samples.length;
		this.samples 	= samples;
	}

		
    /**
     * Returns a string summarizing of the <code>BDFDataRecord</code> object.
     *
     * @return  A summary string
     */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(number);
		buffer.append("(" + channels + ")");
		buffer.append(": {");
		
		for(int i = 0; i < samples.length; i++) {
			buffer.append(samples[i]);
			
			if(i < samples.length-1) {
				buffer.append(",");		
			}
		}
		
		buffer.append("}");
		
		return buffer.toString();
	}
}