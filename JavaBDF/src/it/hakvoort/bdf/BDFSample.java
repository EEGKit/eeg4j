package it.hakvoort.bdf;

/**
 *
 * The <code>BDFSample</code> class represents a sample of a data record of a BDF stream.
 * It conains a sample number and the sample values for each of the channels.
 * 
 * @author	Gido Hakvoort (gido@hakvoort.it)
 * @see  	
 * 
 */
public class BDFSample {
	
    /** number is used as an unique identifier for samples. */
	public final int number;
		
	/** the recorded values of the channels in this sample. */
	public final int values[];
	
    /**
     * Constructs a <code>BDFSample</code> with only one channel.
     * 
	 * @param 	number
	 * 			The number of this data record sample
	 * @param 	value
	 * 			The recorded value of the channel 
	 */
	public BDFSample(int number, int value) {
		this(number, new int[] {value});
	}
	
	/**
     * Constructs a <code>BDFSample</code> with multiple values for multiple channel.
     * 
	 * @param 	number
	 * 			The number of this data record sample
	 * @param 	values
	 * 			The recorded values of the channels
	 */
	public BDFSample(int number, int values[]) {
		this.number 	= number;
		this.values 	= values;
	}

		
    /**
     * Returns a string summarizing of the <code>BDFSample</code> object.
     *
     * @return  A summary string
     */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(number);
		buffer.append("(" + values.length + ")");
		buffer.append(": {");
		
		for(int i = 0; i < values.length; i++) {
			buffer.append(values[i]);
			
			if(i < values.length-1) {
				buffer.append(",");		
			}
		}
		
		buffer.append("}");
		
		return buffer.toString();
	}
}