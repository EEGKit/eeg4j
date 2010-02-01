package it.hakvoort.neuroclient;

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
public class EDFHeader {
	
	// version of this data format (0)
	public String version = null;

	// local patient identification
	public String patient = null;
	
	// local recording identification
	public String recording = null;
	
	// startdate of recording (dd.mm.yy)
	public String startdate = null;
	
	// starttime of recording (hh.mm.ss)
	public String starttime = null;
	
	// number of bytes in header record
	public String length = null;
	
	// reserved
	public String reserved = null;
	
	// number of data records (-1 if unknown)
	public String numrecords = null;
	
	// duration of a data record, in seconds
	public String duration = null;
	
	// number of signals (ns) in data record
	public String numsignals = null;
	
	// ns * label
	public List<String> signal_label = new ArrayList<String>();
	
	// ns * transducer type (e.g. AgAgCl electrode)
	public List<String> signal_transducerType = new ArrayList<String>();
	
	// ns * physical dimension (e.g. uV)
	public List<String> signal_physicalDimension = new ArrayList<String>();
	
	// ns * physical minimum (e.g. -500 or 34)
	public List<String> signal_physicalMinimum = new ArrayList<String>();
	
	// ns * physical maximum (e.g. 500 or 40)
	public List<String> signal_physicalMaximum = new ArrayList<String>();
	
	// ns * digital minimum (e.g. -2048)
	public List<String> signal_digitalMinimum = new ArrayList<String>();
	
	// ns * digital maximum (e.g. 2047)
	public List<String> signal_digitalMaximum = new ArrayList<String>();
	
	// ns * prefiltering (e.g. HP:0.1Hz LP:75Hz)
	public List<String> signal_prefiltering = new ArrayList<String>();
	
	// ns * nr of samples in each data record
	public List<String> signal_numsamples = new ArrayList<String>();
	
	// ns * reserved
	public List<String> signal_reserved = new ArrayList<String>();
		
	public EDFHeader() {
		
	}
	
	public EDFHeader(Properties headerproperties) {
		parseHeaderProperties(headerproperties);
	}
	
	public EDFHeader(String headerLine) {
		parseHeaderLine(headerLine);
	}
	
	private void parseHeaderProperties(Properties properties) {
		Date date = new Date();
		Format dateFormat = new SimpleDateFormat("dd.MM.yy");
		Format timeFormat = new SimpleDateFormat("HH.mm.ss");

		version = properties.getProperty("version", "").trim();
		patient = properties.getProperty("patient", "").trim();
		recording = properties.getProperty("recording", "").trim();

		String tmp_startdate = properties.getProperty("startdate", "").trim();
		if(tmp_startdate.toUpperCase().equals("AUTO")) {
			startdate = dateFormat.format(date);
		} else {
			startdate = tmp_startdate;
		}
		
		String tmp_starttime = properties.getProperty("starttime", "").trim();
		if(tmp_startdate.toUpperCase().equals("AUTO")) {
			starttime = timeFormat.format(date);
		} else {
			starttime = tmp_starttime;
		}
		
		reserved = properties.getProperty("reserved", "").trim();
		numrecords = properties.getProperty("number.of.records", "").trim();
		duration = properties.getProperty("duration", "").trim();
		numsignals = properties.getProperty("number.of.signals", "").trim();
		
		String values = null;
		
		values = properties.getProperty("label", "");
		for(String value : values.split(",")) {
			signal_label.add(value.trim());
		}
		
		values = properties.getProperty("transducer.type", "");
		for(String value : values.split(",")) {
			signal_transducerType.add(value.trim());
		}
		
		values = properties.getProperty("physical.dimension", "");
		for(String value : values.split(",")) {
			signal_physicalDimension.add(value.trim());
		}
		
		values = properties.getProperty("physical.minimum", "");
		for(String value : values.split(",")) {
			signal_physicalMinimum.add(value.trim());
		}
		
		values = properties.getProperty("physical.maximum", "");
		for(String value : values.split(",")) {
			signal_physicalMaximum.add(value.trim());
		}
		
		values = properties.getProperty("digital.minimum", "");
		for(String value : values.split(",")) {
			signal_digitalMinimum.add(value.trim());
		}
		
		values = properties.getProperty("digital.maximum", "");
		for(String value : values.split(",")) {
			signal_digitalMaximum.add(value.trim());
		}
		
		values = properties.getProperty("prefiltering", "");
		for(String value : values.split(",")) {
			signal_prefiltering.add(value.trim());
		}
		
		values = properties.getProperty("number.of.samples", "");
		for(String value : values.split(",")) {
			signal_numsamples.add(value.trim());
		}
				
		values = properties.getProperty("reserved.signal", "");
		for(String value : values.split(",")) {
			signal_reserved.add(value.trim());
		}

		String tmp_length = properties.getProperty("length", "");
		if(tmp_length.toUpperCase().equals("AUTO")) {
			length = String.valueOf(toString().getBytes().length);
		} else {
			length = tmp_length.trim();
		}
	}
	
	private void parseHeaderLine(String header) {
		if(header.length() < 256) {
			System.err.println(String.format("Invalid EDF Header: %s", header));
			return;
		}
		
		version 	= header.substring(0, 8);
		patient 	= header.substring(8, 88);
		recording 	= header.substring(88, 168);
		startdate 	= header.substring(168, 176);
		starttime 	= header.substring(176, 184);
		length 		= header.substring(184, 192);
		reserved 	= header.substring(192, 236);
		numrecords 	= header.substring(236, 244);
		duration	= header.substring(244, 252);
		numsignals 	= header.substring(252, 256);

		int number = Integer.parseInt(numsignals.trim());
		
		int offset = 256;
		for(int i=0, j=1; i<number; i++, j++) {
			signal_label.add(header.substring(offset+(16*i), offset+(16*j)));
		}
		
		offset += (16*number);
		for(int i=0, j=1; i<number; i++, j++) {
			signal_transducerType.add(header.substring(offset+(80*i), offset+(80*j)));
		}

		offset += (80*number);
		for(int i=0, j=1; i<number; i++, j++) {
			signal_physicalDimension.add(header.substring(offset+(8*i), offset+(8*j)));
		}
		
		offset += (8*number);
		for(int i=0, j=1; i<number; i++, j++) {
			signal_physicalMinimum.add(header.substring(offset+(8*i), offset+(8*j)));
		}
		
		offset += (8*number);
		for(int i=0, j=1; i<number; i++, j++) {
			signal_physicalMaximum.add(header.substring(offset+(8*i), offset+(8*j)));
		}
		
		offset += (8*number);
		for(int i=0, j=1; i<number; i++, j++) {
			signal_digitalMinimum.add(header.substring(offset+(8*i), offset+(8*j)));
		}
		
		offset += (8*number);
		for(int i=0, j=1; i<number; i++, j++) {
			signal_digitalMaximum.add(header.substring(offset+(8*i), offset+(8*j)));
		}
		
		offset += (8*number);
		for(int i=0, j=1; i<number; i++, j++) {
			signal_prefiltering.add(header.substring(offset+(80*i), offset+(80*j)));
		}
		
		offset += (80*number);
		for(int i=0, j=1; i<number; i++, j++) {
			signal_numsamples.add(header.substring(offset+(8*i), offset+(8*j)));
		}
		
		offset += (8*number);
		for(int i=0, j=1; i<number; i++, j++) {
			signal_reserved.add(header.substring(offset+(32*i), offset+(32*j)));
		}
	}
	
	public String fillString(String string, int size, char value) {
		char[] target = new char[size];
		
		if(string != null) {
			char[] source = string.toCharArray();
			
			Arrays.fill(target, 0, size, value);
			System.arraycopy(source, 0, target, 0, source.length);
		}
		
		return new String(target);
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
				
		buffer.append(fillString(version, 8, ' '));
		buffer.append(fillString(patient, 80, ' '));
		buffer.append(fillString(recording, 80, ' '));
		buffer.append(fillString(startdate, 8, ' '));
		buffer.append(fillString(starttime, 8, ' '));
		buffer.append(fillString(length, 8, ' '));
		buffer.append(fillString(reserved, 44, ' '));
		buffer.append(fillString(numrecords, 8, ' '));
		buffer.append(fillString(duration, 8, ' '));
		buffer.append(fillString(numsignals, 4, ' '));
		
		if(numsignals != null && !numsignals.isEmpty()) {
			int number = Integer.parseInt(numsignals.trim());
			String value = null;
			
			for(int i = 0; i < number; i++) {
				value = signal_label.size() > i ? signal_label.get(i) : "";
				buffer.append(fillString(value, 16, ' '));
			}
	
			for(int i = 0; i < number; i++) {
				value = signal_transducerType.size() > i ? signal_transducerType.get(i) : "";
				buffer.append(fillString(value, 80, ' '));
			}
			
			for(int i = 0; i < number; i++) {
				value = signal_physicalDimension.size() > i ? signal_physicalDimension.get(i) : "";
				buffer.append(fillString(value, 8, ' '));
			}
			
			for(int i = 0; i < number; i++) {
				value = signal_physicalMinimum.size() > i ? signal_physicalMinimum.get(i) : "";
				buffer.append(fillString(value, 8, ' '));
			}
			
			for(int i = 0; i < number; i++) {
				value = signal_physicalMaximum.size() > i ? signal_physicalMaximum.get(i) : "";
				buffer.append(fillString(value, 8, ' '));
			}
			
			for(int i = 0; i < number; i++) {
				value = signal_digitalMinimum.size() > i ? signal_digitalMinimum.get(i) : "";
				buffer.append(fillString(value, 8, ' '));
			}
			
			for(int i = 0; i < number; i++) {
				value = signal_digitalMaximum.size() > i ? signal_digitalMaximum.get(i) : "";
				buffer.append(fillString(value, 8, ' '));
			}
	
			for(int i = 0; i < number; i++) {
				value = signal_prefiltering.size() > i ? signal_prefiltering.get(i) : "";
				buffer.append(fillString(value, 80, ' '));
			}
			
			for(int i = 0; i < number; i++) {
				value = signal_numsamples.size() > i ? signal_numsamples.get(i) : "";
				buffer.append(fillString(value, 8, ' '));
			}
			
			for(int i = 0; i < number; i++) {
				value = signal_reserved.size() > i ? signal_reserved.get(i) : "";
				buffer.append(fillString(value, 32, ' '));
			}
		}
		
		return buffer.toString();
	}
	
	public void setVersion(String version) {
		this.version = version;
	}

	public void setPatient(String patient) {
		this.patient = patient;
	}

	public void setRecording(String recording) {
		this.recording = recording;
	}

	public void setStartdate(String startdate) {
		this.startdate = startdate;
	}

	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public void setReserved(String reserved) {
		this.reserved = reserved;
	}

	public void setNumrecords(String numrecords) {
		this.numrecords = numrecords;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public void setNumsignals(String numsignals) {
		this.numsignals = numsignals;
	}

	public void addSignal_label(String signalLabel) {
		signal_label.add(signalLabel);
	}

	public void addSignal_transducerType(String signalTransducerType) {
		signal_transducerType.add(signalTransducerType);
	}

	public void addSignal_physicalDimension(String signalPhysicalDimension) {
		signal_physicalDimension.add(signalPhysicalDimension);
	}

	public void addSignal_physicalMinimum(String signalPhysicalMinimum) {
		signal_physicalMinimum.add(signalPhysicalMinimum);
	}

	public void addSignal_physicalMaximum(String signalPhysicalMaximum) {
		signal_physicalMaximum.add(signalPhysicalMaximum);
	}

	public void addSignal_digitalMinimum(String signalDigitalMinimum) {
		signal_digitalMinimum.add(signalDigitalMinimum);
	}

	public void addSignal_digitalMaximum(String signalDigitalMaximum) {
		signal_digitalMaximum.add(signalDigitalMaximum);
	}

	public void addSignal_prefiltering(String signalPrefiltering) {
		signal_prefiltering.add(signalPrefiltering);
	}

	public void addSignal_numsamples(String signalNumsamples) {
		signal_numsamples.add(signalNumsamples);
	}

	public void addSignal_reserved(String signalReserved) {
		signal_reserved.add(signalReserved);
	}
}