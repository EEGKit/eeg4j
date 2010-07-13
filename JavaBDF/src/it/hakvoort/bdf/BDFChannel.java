package it.hakvoort.bdf;

import java.util.Arrays;

/**
 * The <code>BDFChannel</code> class represents a description of the BDFChannel according to the BioSemi Data Format.
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 * 
 */
public class BDFChannel {
	
	// An empty channel header which should be loaded in case of a new channel
	public final static String EMPTY_MAIN_HEADER = "0                                                                                                                                                                                       256                                                         -1      0   ";
	
	// label
	public byte[] label = new byte[16];
	
	// transducer type (e.g. AgAgCl electrode)
	public byte[] transducerType = new byte[80];
	
	// physical dimension (e.g. uV)
	public byte[] physicalDimension = new byte[8];
	
	// physical minimum (e.g. -500 or 34)
	public byte[] physicalMinimum = new byte[8];
	
	// physical maximum (e.g. 500 or 40)
	public byte[] physicalMaximum = new byte[8];
	
	// digital minimum (e.g. -2048)
	public byte[] digitalMinimum = new byte[8];
	
	// digital maximum (e.g. 2047)
	public byte[] digitalMaximum = new byte[8];
	
	// prefiltering (e.g. HP:0.1Hz LP:75Hz)
	public byte[] prefiltering = new byte[80];
	
	// nr of samples in each data record
	public byte[] numSamples = new byte[8];
	
	// reserved
	public byte[] reserved = new byte[32];
	
	public BDFChannel() {

		// load default values
		setLabel("");
		setTransducerType("");
		setPhysicalDimension("");
		setPhysicalMinimum("");
		setPhysicalMaximum("");
		setDigitalMinimum("");
		setDigitalMaximum("");
		setPrefiltering("");
		setNumSamples("512");
		setReserved("");
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
	 * Sets the label header part of this channel.
	 * 
	 * @param 	label
	 * 			A String containing the label data of the channel.
	 * 			label is truncating or padding with whitespace so it has the same length as the old label.
	 */
	public void setLabel(String label) {
		Arrays.fill(this.label, (byte) ' ');
		byte[] bytes = label.getBytes();
		System.arraycopy(bytes, 0, this.label, 0, Math.min(bytes.length, this.label.length));
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
	 * Sets the transducer type information header part of this channel.
	 * 
	 * @param 	transducerType
	 * 			A String containing the transducer type data of the channel.
	 * 			transducer type is truncating or padding with whitespace so it has the same length as the old transducer type.
	 */
	public void setTransducerType(String transducerType) {
		Arrays.fill(this.transducerType, (byte) ' ');
		byte[] bytes = transducerType.getBytes();
		System.arraycopy(bytes, 0, this.transducerType, 0, Math.min(bytes.length, this.transducerType.length));
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
	 * Sets the physical dimension information header part of this channel.
	 * 
	 * @param 	physicalDimension
	 * 			A String containing the physical dimension data of the channel.
	 * 			physical dimension is truncating or padding with whitespace so it has the same length as the old physical dimension.
	 */
	public void setPhysicalDimension(String physicalDimension) {
		Arrays.fill(this.physicalDimension, (byte) ' ');
		byte[] bytes = physicalDimension.getBytes();
		System.arraycopy(bytes, 0, this.physicalDimension, 0, Math.min(bytes.length, this.physicalDimension.length));
	}	
	
	/**
	 * Returns the physical minimum of this channel.
	 * e.g.: -262144
	 * 
	 * @return 	the physical minimum
	 */
	public String getPhysicalMinimum() {
		return new String(physicalMinimum).trim();
	}

	/**
	 * Sets the physical minimum information header part of this channel.
	 * 
	 * @param 	physicalMinimum
	 * 			A String containing the physical minimum data of the channel.
	 * 			physical minimum is truncating or padding with whitespace so it has the same length as the old physical minimum.
	 */
	public void setPhysicalMinimum(String physicalMinimum) {
		Arrays.fill(this.physicalMinimum, (byte) ' ');
		byte[] bytes = physicalMinimum.getBytes();
		System.arraycopy(bytes, 0, this.physicalMinimum, 0, Math.min(bytes.length, this.physicalMinimum.length));
	}		
	
	/**
	 * Returns the physical maximum of this channel.
	 * e.g.: 262143
	 * 
	 * @return 	the physical maximum
	 */
	public String getPhysicalMaximum() {
		return new String(physicalMaximum).trim();
	}

	/**
	 * Sets the physical maximum information header part of this channel.
	 * 
	 * @param 	physicalMaximum
	 * 			A String containing the physical maximum data of the channel.
	 * 			physical maximum is truncating or padding with whitespace so it has the same length as the old physical maximum.
	 */
	public void setPhysicalMaximum(String physicalMaximum) {
		Arrays.fill(this.physicalMaximum, (byte) ' ');
		byte[] bytes = physicalMaximum.getBytes();
		System.arraycopy(bytes, 0, this.physicalMaximum, 0, Math.min(bytes.length, this.physicalMaximum.length));
	}
	
	/**
	 * Returns the digital minimum of this channel.
	 * e.g.: -8388608
	 * 
	 * @return 	the digital minimum
	 */
	public String getDigitalMinimum() {
		return new String(digitalMinimum).trim();
	}

	/**
	 * Sets the digital minimum information header part of this channel.
	 * 
	 * @param 	digitalMinimum
	 * 			A String containing the digital minimum data of the channel.
	 * 			digital minimum is truncating or padding with whitespace so it has the same length as the old digital minimum.
	 */
	public void setDigitalMinimum(String digitalMinimum) {
		Arrays.fill(this.digitalMinimum, (byte) ' ');
		byte[] bytes = digitalMinimum.getBytes();
		System.arraycopy(bytes, 0, this.digitalMinimum, 0, Math.min(bytes.length, this.digitalMinimum.length));
	}
	
	/**
	 * Returns the digital maximum of this channel.
	 * e.g.: 8388607
	 * 
	 * @return 	the digital maximum
	 */
	public String getDigitalMaximum() {
		return new String(digitalMaximum).trim();
	}

	/**
	 * Sets the digital maximum information header part of this channel.
	 * 
	 * @param 	digitalMaximum
	 * 			A String containing the digital maximum data of the channel.
	 * 			digital maximum is truncating or padding with whitespace so it has the same length as the old digital maximum.
	 */
	public void setDigitalMaximum(String digitalMaximum) {
		Arrays.fill(this.digitalMaximum, (byte) ' ');
		byte[] bytes = digitalMaximum.getBytes();
		System.arraycopy(bytes, 0, this.digitalMaximum, 0, Math.min(bytes.length, this.digitalMaximum.length));
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
	 * Sets the prefiltering information header part of this channel.
	 * 
	 * @param 	prefiltering
	 * 			A String containing the prefiltering data of the channel.
	 * 			prefiltering is truncating or padding with whitespace so it has the same length as the old prefiltering.
	 */
	public void setPrefiltering(String prefiltering) {
		Arrays.fill(this.prefiltering, (byte) ' ');
		byte[] bytes = prefiltering.getBytes();
		System.arraycopy(bytes, 0, this.prefiltering, 0, Math.min(bytes.length, this.prefiltering.length));
	}	
	
	/**
	 * Returns the number of samples in this record. This is also an indication of the sample rate of this channel.
	 * e.g.: 512.
	 * 
	 * @return	the number of samples for this record
	 */
	public String getNumSamples() {
		return new String(numSamples).trim();
	}

	/**
	 * Sets the number of samples information header part of this channel.
	 * 
	 * @param 	numSamples
	 * 			A String containing the number of samples data of the channel.
	 * 			number of samples is truncating or padding with whitespace so it has the same length as the old number of samples.
	 */
	public void setNumSamples(String numSamples) {
		Arrays.fill(this.numSamples, (byte) ' ');
		byte[] bytes = numSamples.getBytes();
		System.arraycopy(bytes, 0, this.numSamples, 0, Math.min(bytes.length, this.numSamples.length));
	}
	
	/**
	 * Returns some reserved information.
	 * 
	 * @return 	reserved information
	 */
	public String getReserved() {
		return new String(reserved).trim();
	}
	
	/**
	 * Sets the reserved information header part of this channel.
	 * 
	 * @param 	reserved
	 * 			A String containing the reserved data of the channel.
	 * 			reserved is truncating or padding with whitespace so it has the same length as the old reserved.
	 */
	public void setReserved(String reserved) {
		Arrays.fill(this.reserved, (byte) ' ');
		byte[] bytes = reserved.getBytes();
		System.arraycopy(bytes, 0, this.reserved, 0, Math.min(bytes.length, this.reserved.length));
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