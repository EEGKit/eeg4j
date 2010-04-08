package it.hakvoort.bdf;

import java.nio.ByteBuffer;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The <code>BDFHeader</code> class represents a BioSemi Data Format Header.
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 * 
 */
public class BDFHeader {
	
	// version of this data format
	private byte[] version = new byte[8];

	// local patient identification
	private byte[] patient = new byte[80];
	
	// local recording identification
	private byte[] recording = new byte[80];
	
	// startdate of recording (dd.mm.yy)
	private byte[] startdate = new byte[8];
	
	// starttime of recording (hh.mm.ss)
	private byte[] starttime = new byte[8];
	
	// number of bytes in header record
	private byte[] length = new byte[8];
	
	// reserved
	private byte[] reserved = new byte[44];
	
	// number of data records (-1 if unknown)
	private byte[] numRecords = new byte[8];
	
	// duration of a data record, in seconds
	private byte[] duration = new byte[8];
	
	// number of channels (ns) in data record
	private byte[] numChannels = new byte[4];
	
	// ns (the channels)
	private List<BDFChannel> channels = new ArrayList<BDFChannel>();
	
	public BDFHeader() {
		
	}
	
	/**
	 * Sets the main header part of this object.
	 * 
	 * @param 	main
	 * 			A byte array containing the main header data.
	 * 
	 */
	public void setMainHeader(byte[] main) throws BDFException {
		if(main.length != 256) {
			throw new BDFException(String.format("Invalid BDF Main Header: %s", main));
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
	
	/**
	 * Sets the channel header part of this object.
	 * 
	 * @param 	channel
	 * 			A byte array containing the header data of the channels.
	 */
	public void setChannelHeader(byte[] channel) throws BDFException {
		if(channel.length != 256 * getNumChannels()) {
			throw new BDFException(String.format("Invalid BDF Channel Header: %s", channel));
		}
		
		ByteBuffer buffer = ByteBuffer.wrap(channel);

		for(int c = 0, offset = getNumChannels()-1; c < getNumChannels(); c++, offset--) {
			BDFChannel bdfChannel = new BDFChannel();
			
			buffer.position(c*16);
			buffer.get(bdfChannel.label);
			
			buffer.position(buffer.position() + offset*16 + c*80);
			buffer.get(bdfChannel.transducerType);
			
			buffer.position(buffer.position() + offset*80 + c*8);
			buffer.get(bdfChannel.physicalDimension);
			
			buffer.position(buffer.position() + offset*8 + c*8);
			buffer.get(bdfChannel.physicalMinimum);
			
			buffer.position(buffer.position() + offset*8 + c*8);
			buffer.get(bdfChannel.physicalMaximum);
			
			buffer.position(buffer.position() + offset*8 + c*8);
			buffer.get(bdfChannel.digitalMinimum);
			
			buffer.position(buffer.position() + offset*8 + c*8);
			buffer.get(bdfChannel.digitalMaximum);
			
			buffer.position(buffer.position() + offset*8 + c*80);
			buffer.get(bdfChannel.prefiltering);
			
			buffer.position(buffer.position() + offset*80 + c*8);
			buffer.get(bdfChannel.numSamples);
			
			buffer.position(buffer.position() + offset*8 + c*32);
			buffer.get(bdfChannel.reserved);
			
			this.channels.add(bdfChannel);
			buffer.rewind();
		}
	}
	
    /**
     * Returns a string summarizing this object.
     *
     * @return  A summary string
     */
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
	
	/**
	 * Returns the version of the object.
	 * 
	 * @return	the version.
	 */
	public String getVersion() {
		return new String(version).trim();
	}

	/**
	 * Returns the patient information of this object.
	 * 
	 * @return	the patient information.
	 */
	public String getPatient() {
		return new String(patient).trim();
	}
	
	/**
	 * Returns the recording information of this object.
	 * 
	 * @return	the recording information.
	 */
	public String getRecording() {
		return new String(recording).trim();
	}
	
	/**
	 * Returns the start date and time of this object.
	 * 
	 * @return	the start date and time.
	 */
	public Date getStartTime() {
		Format format = new SimpleDateFormat("dd.MM.yy-HH.mm.ss");
		
		try {
			return (Date) format.parseObject(new String(startdate).trim() + "-" + new String(starttime).trim());
		} catch (ParseException e) {
			return null;
		} 
	}
	
	/**
	 * Returns the length of this object. {(N + 1) * 256} bytes, where N is the number of channels in this object.
	 * 
	 * @return	the lenght of this object.
	 */
	public int getLength() {
		return Integer.parseInt(new String(length).trim());
	}
	
	/**
	 * Returns the reserved header part of this object, which can be used to store aditional information.
	 * 
	 * @return	the reserved part of this object.
	 */
	public String getReserved() {
		return new String(recording).trim();
	}
	
	/**
	 * Returns the number of records in the object.
	 * 
	 * @return	the number of records, or -1 if unknown.
	 */
	public int getNumRecords() {
		return Integer.parseInt(new String(numRecords).trim());
	}
	
	/**
	 * Returns the duration of this object in seconds.
	 * 
	 * @return	the duration of this object.
	 */
	public int getDuration() {
		return Integer.parseInt(new String(duration).trim());
	}
	
	/**
	 * Returns the number of channels in this object.
	 * 
	 * @return	the number of channels in this object.
	 */
	public int getNumChannels() {
		return Integer.parseInt(new String(numChannels).trim());
	}
	
	/**
	 * Returns the requested channel in this object.
	 * 
	 * @param 	index
	 * 			index of the requested channel
	 * @return	the requested channel
	 */
	public BDFChannel getChannel(int index) {
		if(index < 0 || index >= channels.size()) {
			return null;
		}
		
		return channels.get(index);
	}
	
	public class BDFChannel {
		
		// label
		private byte[] label= new byte[16];
		
		// transducer type (e.g. AgAgCl electrode)
		private byte[] transducerType= new byte[80];
		
		// physical dimension (e.g. uV)
		private byte[] physicalDimension= new byte[8];
		
		// physical minimum (e.g. -500 or 34)
		private byte[] physicalMinimum= new byte[8];
		
		// physical maximum (e.g. 500 or 40)
		private byte[] physicalMaximum= new byte[8];
		
		// digital minimum (e.g. -2048)
		private byte[] digitalMinimum= new byte[8];
		
		// digital maximum (e.g. 2047)
		private byte[] digitalMaximum= new byte[8];
		
		// prefiltering (e.g. HP:0.1Hz LP:75Hz)
		private byte[] prefiltering= new byte[80];
		
		// nr of samples in each data record
		private byte[] numSamples= new byte[8];
		
		// reserved
		private byte[] reserved = new byte[32];
		
		public BDFChannel() {
			
		}
		
		/**
		 * Returns the label information of this channel.
		 * e.g.: Oz, Pz.
		 * 
		 * @return	the label for this channel
		 */
		public String getLabel() {
			return new String(label).trim();
		}

		/**
		 * Returns the transducer type information of this channel.
		 * e.g.: active electrode.
		 * 
		 * @return	the transducer type
		 */
		public String getTransducerType() {
			return new String(transducerType).trim();
		}

		/**
		 * Returns the physical dimension of this channel. 
		 * e.g.: uV, Ohm.
		 * 
		 * @return	the physical dimension
		 */
		public String getPhysicalDimension() {
			return new String(physicalDimension).trim();
		}

		/**
		 * Returns the physical minimum of this channel.
		 * e.g.: -262144
		 * 
		 * @return 	the physical minimum
		 */
		public int getPhysicalMinimum() {
			return Integer.parseInt(new String(physicalMinimum).trim());
		}

		/**
		 * Returns the physical maximum of this channel.
		 * e.g.: 262143
		 * 
		 * @return 	the physical maximum
		 */
		public int getPhysicalMaximum() {
			return Integer.parseInt(new String(physicalMaximum).trim());
		}

		/**
		 * Returns the digital minimum of this channel.
		 * e.g.: -8388608
		 * 
		 * @return 	the digital minimum
		 */
		public int getDigitalMinimum() {
			return Integer.parseInt(new String(digitalMinimum).trim());
		}

		/**
		 * Returns the digital maximum of this channel.
		 * e.g.: 8388607
		 * 
		 * @return 	the digital maximum
		 */
		public int getDigitalMaximum() {
			return Integer.parseInt(new String(digitalMaximum).trim());
		}

		/**
		 * Returns the type of prefiltering used for this channel.
		 * e.g.: HP: DC; LP: 104 Hz
		 * 
		 * @return 	the prefiltering
		 */
		public String getPrefiltering() {
			return new String(prefiltering).trim();
		}

		/**
		 * Returns the number of samples in this record. This is also an indication of the sample rate of this channel.
		 * e.g.: 512.
		 * 
		 * @return	the number of samples for this record
		 */
		public int getNumSamples() {
			return Integer.parseInt(new String(numSamples).trim());
		}
		
		/**
		 * Returns some reserved information.
		 * 
		 * @return 	reserved information
		 */
		public String getReserved() {
			return new String(reserved).trim();
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