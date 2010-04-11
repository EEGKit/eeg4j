package it.hakvoort.neuroclient;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 * 
 */
public class NeuroServerPacket {
	
	public int packetCounter;
	public int channelCount;
	public int samples[];
	
	public NeuroServerPacket(int packetCounter, int sample) {
		this(packetCounter, 1, sample);
	}
	
	public NeuroServerPacket(int packetCounter, int samples[]) {
		this(packetCounter, samples.length, samples);
	}
	
	public NeuroServerPacket(int packetCounter, int channelCount, int sample) {
		this.packetCounter 	= packetCounter;
		this.channelCount 	= channelCount;
		this.samples	 	= new int[] {sample};
	}
	
	public NeuroServerPacket(int packetCounter, int channelCount, int samples[]) {
		this.packetCounter 	= packetCounter;
		this.channelCount 	= channelCount;
		this.samples 		= samples;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(packetCounter);
		buffer.append(" ");
		buffer.append(channelCount);
		
		for(int sample : samples) {
			buffer.append(" ");
			buffer.append(sample);
		}
		
		return buffer.toString();
	}
}