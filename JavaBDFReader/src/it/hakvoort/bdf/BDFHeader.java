package it.hakvoort.bdf;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 * 
 */
public class BDFHeader {
	
	// version of this data format
	public byte[] version = new byte[8];

	// local patient identification
	public byte[] patient = new byte[80];
	
	// local recording identification
	public byte[] recording = new byte[80];
	
	// startdate of recording (dd.mm.yy)
	public byte[] startdate = new byte[8];
	
	// starttime of recording (hh.mm.ss)
	public byte[] starttime = new byte[8];
	
	// number of bytes in header record
	public byte[] length = new byte[8];
	
	// reserved
	public byte[] reserved = new byte[44];
	
	// number of data records (-1 if unknown)
	public byte[] numRecords = new byte[8];
	
	// duration of a data record, in seconds
	public byte[] duration = new byte[8];
	
	// number of channels (ns) in data record
	public byte[] numChannels = new byte[4];
	
	// ns (the channels)
	public List<BDFChannel> channels = new ArrayList<BDFChannel>();
	
	public BDFHeader() {
		
	}
	
	public BDFHeader(byte[] main) {
		setMainHeader(main);
	}
	
	public void setMainHeader(byte[] main) {
		if(main.length != 256) {
			System.err.println(String.format("Invalid BDF Main Header: %s", main));
			return;
		}
		
		ByteBuffer buffer = ByteBuffer.wrap(main);
		
		buffer.get(version);
		buffer.get(patient);		
		buffer.get(recording);
		buffer.get(startdate);
		buffer.get(starttime);
		buffer.get(length);
		buffer.get(reserved);
		buffer.get(numRecords);
		buffer.get(duration);
		buffer.get(numChannels);
	}
	
	public void setChannelHeader(byte[] channelData) {
		if(channelData.length != 256 * getNumChannels()) {
			System.err.println(String.format("Invalid BDF Signals Header: %s", channelData));
			return;
		}
		
		ByteBuffer buffer = ByteBuffer.wrap(channelData);

		for(int c = 0, offset = getNumChannels()-1; c < getNumChannels(); c++, offset--) {
			BDFChannel channel = new BDFChannel();
			
			buffer.position(c*16);
			buffer.get(channel.label);
			
			buffer.position(buffer.position() + offset*16 + c*80);
			buffer.get(channel.transducerType);
			
			buffer.position(buffer.position() + offset*80 + c*8);
			buffer.get(channel.physicalDimension);
			
			buffer.position(buffer.position() + offset*8 + c*8);
			buffer.get(channel.physicalMinimum);
			
			buffer.position(buffer.position() + offset*8 + c*8);
			buffer.get(channel.physicalMaximum);
			
			buffer.position(buffer.position() + offset*8 + c*8);
			buffer.get(channel.digitalMinimum);
			
			buffer.position(buffer.position() + offset*8 + c*8);
			buffer.get(channel.digitalMaximum);
			
			buffer.position(buffer.position() + offset*8 + c*80);
			buffer.get(channel.prefiltering);
			
			buffer.position(buffer.position() + offset*80 + c*8);
			buffer.get(channel.numSamples);
			
			buffer.position(buffer.position() + offset*8 + c*32);
			buffer.get(channel.reserved);
			
			channels.add(channel);
			buffer.rewind();
		}
	}
	
	@Override
	public String toString() {
		ByteBuffer buffer = ByteBuffer.allocate(getLength());

		buffer.put(version);
		buffer.put(patient);
		buffer.put(recording);
		buffer.put(startdate);
		buffer.put(starttime);
		buffer.put(length);
		buffer.put(reserved);
		buffer.put(numRecords);
		buffer.put(duration);
		buffer.put(numChannels);
		
		for(int c = 0, offset = getNumChannels()-1; c < channels.size(); c++, offset--) {
			BDFChannel channel = channels.get(c);
			
			buffer.position(256 + c*16);
			buffer.put(channel.label);
			
			buffer.position(buffer.position() + offset*16 + c*80);
			buffer.put(channel.transducerType);
			
			buffer.position(buffer.position() + offset*80 + c*8);
			buffer.put(channel.physicalDimension);
			
			buffer.position(buffer.position() + offset*8 + c*8);
			buffer.put(channel.physicalMinimum);
			
			buffer.position(buffer.position() + offset*8 + c*8);
			buffer.put(channel.physicalMaximum);
			
			buffer.position(buffer.position() + offset*8 + c*8);
			buffer.put(channel.digitalMinimum);
			
			buffer.position(buffer.position() + offset*8 + c*8);
			buffer.put(channel.digitalMaximum);
			
			buffer.position(buffer.position() + offset*8 + c*80);
			buffer.put(channel.prefiltering);
			
			buffer.position(buffer.position() + offset*80 + c*8);
			buffer.put(channel.numSamples);
			
			buffer.position(buffer.position() + offset*8 + c*32);
			buffer.put(channel.reserved);
			
			buffer.rewind();
		}
		
		return new String(buffer.array());
	}
	
	public int getLength() {
		return Integer.parseInt(new String(length).trim());
	}
	
	public int getNumChannels() {
		return Integer.parseInt(new String(numChannels).trim());
	}
	
	public int getNumRecords() {
		return Integer.parseInt(new String(numRecords).trim());
	}
	
	public class BDFChannel {
		
		// label
		public byte[] label= new byte[16];
		
		// transducer type (e.g. AgAgCl electrode)
		public byte[] transducerType= new byte[80];
		
		// physical dimension (e.g. uV)
		public byte[] physicalDimension= new byte[8];
		
		// physical minimum (e.g. -500 or 34)
		public byte[] physicalMinimum= new byte[8];
		
		// physical maximum (e.g. 500 or 40)
		public byte[] physicalMaximum= new byte[8];
		
		// digital minimum (e.g. -2048)
		public byte[] digitalMinimum= new byte[8];
		
		// digital maximum (e.g. 2047)
		public byte[] digitalMaximum= new byte[8];
		
		// prefiltering (e.g. HP:0.1Hz LP:75Hz)
		public byte[] prefiltering= new byte[80];
		
		// nr of samples in each data record
		public byte[] numSamples= new byte[8];
		
		// reserved
		public byte[] reserved = new byte[32];
		
		public BDFChannel() {
		
		}
		
		public int getNumSamples() {
			return Integer.parseInt(new String(numSamples).trim());
		}
		
		@Override
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			
			buffer.append(new String(label));
			buffer.append(new String(transducerType));
			buffer.append(new String(physicalDimension));
			buffer.append(new String(physicalMinimum));
			buffer.append(new String(physicalMaximum));
			buffer.append(new String(digitalMinimum));
			buffer.append(new String(digitalMaximum));
			buffer.append(new String(prefiltering));
			buffer.append(new String(numSamples));
			buffer.append(new String(reserved));
			
			return buffer.toString();
		}
	}
}