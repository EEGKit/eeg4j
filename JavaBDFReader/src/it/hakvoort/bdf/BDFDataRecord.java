package it.hakvoort.bdf;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 * 
 */
public class BDFDataRecord {
	
	public int recordCounter;
	public int channelCount;
	public int samples[];
	
	public BDFDataRecord(int recordCounter, int sample) {
		this(recordCounter, 1, sample);
	}
	
	public BDFDataRecord(int recordCounter, int samples[]) {
		this(recordCounter, samples.length, samples);
	}
	
	public BDFDataRecord(int recordCounter, int channelCount, int sample) {
		this.recordCounter 	= recordCounter;
		this.channelCount 	= channelCount;
		this.samples	 	= new int[] {sample};
	}
	
	public BDFDataRecord(int recordCounter, int channelCount, int samples[]) {
		this.recordCounter 	= recordCounter;
		this.channelCount 	= channelCount;
		this.samples 		= samples;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(recordCounter);
		buffer.append("(" + channelCount + ")");
		buffer.append(": {");
		for(int sample : samples) {
			buffer.append(sample);
			buffer.append(",");
		}
		buffer.append("}");
		
		return buffer.toString();
	}
}