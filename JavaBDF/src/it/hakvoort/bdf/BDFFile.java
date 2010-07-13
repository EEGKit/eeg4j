package it.hakvoort.bdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class BDFFile {
	
	// the actual bdf file
	private File file = null;
	
	// the current header of the bdf file
	private BDFHeader header = null;
	
	// the reader for this bdf file
	private BDFReader reader = null;
	
	// the writer for this bdf file
	private BDFWriter writer = null;
	
	public BDFFile(String pathname, boolean open) throws IOException, BDFException {
		file = new File(pathname);
		
		if(file.exists() && open) {
			loadHeader();
		} else if(!open) {
			file.createNewFile();
		} else {
			throw new FileNotFoundException();
		}	
	}
	
	private void loadHeader() throws IOException, BDFException {
		InputStream inputStream = new FileInputStream(file);
		
		// create a BDFHeader
		header = new BDFHeader();
		
		// get the main header part
		byte[] main = new byte[256];				
		inputStream.read(main);
		header.loadMainHeader(main);
		
		try {
			
			// get the channel header part
			byte[] signals = new byte[256 * Integer.parseInt(header.getNumChannels())];
			inputStream.read(signals);
			header.loadChannelHeader(signals);
			
		} catch(NumberFormatException e) {
			throw new BDFException("Invalid number of channels in main header");
		}

		inputStream.close();
	}
	
	public int getNumChannels() {
		return header.computeNumChannels();
	}
	
	public int getSampleRate() {
		if(header.computeNumChannels() == 0) {
			return -1;
		} else {
			return Integer.parseInt(header.getChannel(0).getNumSamples());
		}
	}
	
	public BDFHeader getHeader() {
		return header;
	}
	
	public void setHeader(BDFHeader header) {
		this.header = header;		
	}
	
	public File getFile() {
		return file;
	}
	
	public BDFWriter getWriter() {
		if(writer == null) {
			writer = new BDFWriter(this);
		}
		
		return writer;
	}
	
	public BDFReader getReader() {
		if(reader == null) {
			reader = new BDFReader(this);
		}
		
		return reader;
	}
	
	public static BDFFile open(String pathname) throws IOException, BDFException {
		return new BDFFile(pathname, true);
	}
	
	public static BDFFile create(String pathname) throws IOException, BDFException {
		return new BDFFile(pathname, false);
	}
}
