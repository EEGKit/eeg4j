package it.hakvoort.neuroclient;

import java.nio.ByteBuffer;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 * 
 */
public class EDFHeader implements NeuroServerHeader {
	
	public static byte SPACE = 0x20;
	
	// if this is a valid header
	public boolean valid = false;
	
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
	public List<EDFChannel> channels = new ArrayList<EDFChannel>();

	public EDFHeader() {
		
	}
	
	public EDFHeader(byte[] main) {
		setMainHeader(main);
	}

	public EDFHeader(Properties properties) {
		setMainHeader(properties);
		setChannelHeader(properties);
	}
	
	public void setMainHeader(byte[] main) {
		if(main.length != 256) {
			System.err.println(String.format("Invalid EDF main header: %s", new String(main)));
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
		
		valid = true;
	}
	
	public void setChannelHeader(byte[] channelData) {
		if(!isValid()) {
			System.err.println(String.format("Main header should be set before setting channel header"));
			return;	
		}
		
		if(channelData.length != 256 * getNumChannels()) {
			System.err.println(String.format("Invalid EDF channel header: %s", new String(channelData)));
			return;
		}
		
		ByteBuffer buffer = ByteBuffer.wrap(channelData);

		for(int c = 0, offset = getNumChannels()-1; c < getNumChannels(); c++, offset--) {
			EDFChannel channel = new EDFChannel();
			
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
	
	private void setMainHeader(Properties properties) {
		Date date = new Date();
		Format dateFormat = new SimpleDateFormat("dd.MM.yy");
		Format timeFormat = new SimpleDateFormat("HH.mm.ss");

		String value = null;
		
		value = properties.getProperty("version", "").trim();
		version = toByteArray(value, 8, SPACE);
		
		value = properties.getProperty("patient", "").trim();
		patient = toByteArray(value, 80, SPACE);
		
		value = properties.getProperty("recording", "").trim();
		recording = toByteArray(value, 80, SPACE);
		
		value = properties.getProperty("startdate", "").trim();
		if(value.toUpperCase().equals("AUTO")) {
			startdate = toByteArray(dateFormat.format(date), 8, SPACE);
		} else {
			startdate = toByteArray(value, 8, SPACE);
		}
		
		value = properties.getProperty("starttime", "").trim();
		if(value.toUpperCase().equals("AUTO")) {
			starttime = toByteArray(timeFormat.format(date), 8, SPACE);
		} else {
			starttime = toByteArray(value, 8, SPACE);
		}
		
		value = properties.getProperty("reserved", "").trim();
		reserved = toByteArray(value, 44, SPACE);

		value = properties.getProperty("duration", "").trim();
		duration = toByteArray(value, 8, SPACE);
		
		value = properties.getProperty("number.of.records", "").trim();
		numRecords = toByteArray(value, 8, SPACE);
		
		value = properties.getProperty("number.of.channels", "").trim();
		numChannels = toByteArray(value, 4, SPACE);
		
		value = properties.getProperty("length", "");
		if(value.toUpperCase().equals("AUTO")) {
			length = toByteArray(String.valueOf((getNumChannels()+1)*256), 8, SPACE);
		} else {
			length = toByteArray(value, 8, SPACE);;
		}
		
		valid = true;
	}
	
	private void setChannelHeader(Properties properties) {
		if(!isValid()) {
			System.err.println(String.format("Main header should be set before setting channel header"));
			return;
		}
		
		String[] label 				= properties.getProperty("label", "").split(",");
		String[] transducerType 	= properties.getProperty("transducer.type", "").split(",");
		String[] physicalDimension 	= properties.getProperty("physical.dimension", "").split(",");
		String[] physicalMinimum 	= properties.getProperty("physical.minimum", "").split(",");
		String[] physicalMaximum 	= properties.getProperty("physical.maximum", "").split(",");
		String[] digitalMinimum 	= properties.getProperty("digital.minimum", "").split(",");
		String[] digitalMaximum 	= properties.getProperty("digital.maximum", "").split(",");
		String[] prefiltering 		= properties.getProperty("prefiltering", "").split(",");
		String[] numSamples 		= properties.getProperty("number.of.samples", "").split(",");
		String[] reserved 			= properties.getProperty("reserved.signal", "").split(",");
		
		boolean complete = true;
		
		if(label.length < getNumChannels()) {
			System.err.println(String.format("Number of channels is set to %s, but only %s of 'label'.", getNumChannels(), label.length));
			complete = false;
		}
		
		if(transducerType.length < getNumChannels()) {
			System.err.println(String.format("Number of channels is set to %s, but only %s of 'transducer.type'.", getNumChannels(), transducerType.length));
			complete = false;
		}

		if(physicalDimension.length < getNumChannels()) {
			System.err.println(String.format("Number of channels is set to %s, but only %s of 'physical.dimension'.", getNumChannels(), physicalDimension.length));
			complete = false;
		}

		if(physicalMinimum.length < getNumChannels()) {
			System.err.println(String.format("Number of channels is set to %s, but only %s of 'physical.minimum'.", getNumChannels(), physicalMinimum.length));
			complete = false;
		}
		
		if(physicalMaximum.length < getNumChannels()) {
			System.err.println(String.format("Number of channels is set to %s, but only %s of 'physical.maximum'.", getNumChannels(), physicalMaximum.length));
			complete = false;
		}
		
		if(digitalMinimum.length < getNumChannels()) {
			System.err.println(String.format("Number of channels is set to %s, but only %s of 'digital.minimum'.", getNumChannels(), digitalMinimum.length));
			complete = false;
		}
		
		if(digitalMaximum.length < getNumChannels()) {
			System.err.println(String.format("Number of channels is set to %s, but only %s of 'digital.maximum'.", getNumChannels(), digitalMaximum.length));
			complete = false;
		}
		
		if(prefiltering.length < getNumChannels()) {
			System.err.println(String.format("Number of channels is set to %s, but only %s of 'prefiltering'.", getNumChannels(), prefiltering.length));
			complete = false;
		}
		
		if(numSamples.length < getNumChannels()) {
			System.err.println(String.format("Number of channels is set to %s, but only %s of 'number.of.samples'.", getNumChannels(), numSamples.length));
			complete = false;
		}

		if(reserved.length < getNumChannels()) {
			System.err.println(String.format("Number of channels is set to %s, but only %s of 'reserved'.", getNumChannels(), reserved.length));
			complete = false;
		}
		
		if(!complete) {
			valid = false;
			return;
		}
		
		for(int i = 0; i < getNumChannels(); i++) {
			EDFChannel channel = new EDFChannel();
			
			channel.label 				= toByteArray(label[i].trim(), 16, SPACE);			
			channel.transducerType 		= toByteArray(transducerType[i].trim(), 80, SPACE);
			channel.physicalDimension 	= toByteArray(physicalDimension[i].trim(), 8, SPACE);
			channel.physicalMinimum 	= toByteArray(physicalMinimum[i].trim(), 8, SPACE);
			channel.physicalMaximum 	= toByteArray(physicalMaximum[i].trim(), 8, SPACE);
			channel.digitalMinimum 		= toByteArray(digitalMinimum[i].trim(), 8, SPACE);
			channel.digitalMaximum 		= toByteArray(digitalMaximum[i].trim(), 8, SPACE);
			channel.prefiltering 		= toByteArray(prefiltering[i].trim(), 80, SPACE);
			channel.numSamples 			= toByteArray(numSamples[i].trim(), 8, SPACE);
			channel.reserved 			= toByteArray(reserved[i].trim(), 32, SPACE);
			
			channels.add(channel);
		}
	}

	public int getLength() {
		return Integer.parseInt(new String(length).trim());
	}
	
	public int getNumChannels() {
		return Integer.parseInt(new String(numChannels).trim());
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
			EDFChannel channel = channels.get(c);
			
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

	public byte[] toByteArray(String string, int size, byte value) {
		byte[] target = new byte[size];
		
		if(string != null) {
			byte[] source = string.getBytes();
			
			Arrays.fill(target, 0, size, value);
			System.arraycopy(source, 0, target, 0, source.length);
		}
		
		return target;
	}
	
	@Override
	public String getData() {
		return this.toString();
	}
	
	@Override
	public boolean isValid() {
		return valid;
	}
	
	public class EDFChannel {
		
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
		
		public EDFChannel() {
		
		}
		
		public int getNumSamples() {
			return Integer.parseInt(new String(numSamples).trim());
		}
	}
}