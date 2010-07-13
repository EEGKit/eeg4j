package it.hakvoort.bdf;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BDFWriter {
	
	// the bdf file
	private BDFFile bdf = null;
	
	// the fileWriter, writing incomming data to the file.
	private BDFFileWriter fileWriter = new BDFFileWriter();
	
	// the bdf file outputstream
	private FileOutputStream outputStream = null;
	
	// if the file and data writer are running
	private boolean running = false;
	
	// received samples
	private ConcurrentLinkedQueue<BDFSample> samples = new ConcurrentLinkedQueue<BDFSample>();
	
	public BDFWriter(BDFFile bdf) {
		this.bdf = bdf;
	}
		
	public boolean isRunning() {
		return this.running;
	}
	
	public void start() {
		running = true;
		fileWriter.start();
	}
	
	public void stop() {
		running = false;
	}
	
	public void addSample(BDFSample sample) {
		samples.add(sample);
		
		synchronized(samples) {
			samples.notifyAll();
		}
	}
	
	private class BDFFileWriter extends Thread {
		
		// the number of channels
		private int numChannels;
		
		// the sample rate
		private int sampleRate;
		
		// the record to store
		private int[][] record;
		
		public BDFFileWriter() {
			
		}
		
		public void run() {
			
			try {
				// open output stream
				outputStream = new FileOutputStream(bdf.getFile());
				
				// write header
				writeHeader();
				
			} catch (IOException e) {
				e.printStackTrace();
				running = false;
			} catch (BDFException e) {
				e.printStackTrace();
				running = false;
			}
			
			numChannels = bdf.getNumChannels();
			
			if(numChannels == 0) {
				System.err.println(String.format("NO BDFChannels were set."));
				running = false;
			}
			
			if(!running) {
				return;
			}
			
			sampleRate 	= bdf.getSampleRate();
			
			// two dimensional array with size of sampleRate x numChannels
			record = new int[numChannels][sampleRate];
			
			// total samples
			int sampleCount = 0;

			while(running || !samples.isEmpty()) {
				
				synchronized(samples) {
					while(samples.isEmpty()) {
						try {
							samples.wait();
						} catch(InterruptedException e) {}
					}
				
					while(!samples.isEmpty()) {
						
						// get next sample
						BDFSample sample = samples.poll();
						
						// check if sample has the same number of channels, else replace it
						if(sample.values.length != numChannels) {
							System.err.println("Number of channels do not match.");
							sample = new BDFSample(sample.number, new int[numChannels]);
						}
						
						// store samples in record
						for(int c = 0; c < numChannels; c++) {
							record[c][sampleCount] = sample.values[c];
						}
						
						// update counter
						sampleCount++;
						
						// if record is full, write record to file
						if(sampleCount >= sampleRate) {
							try {
								writeRecord();
							} catch (IOException e) {
								e.printStackTrace();
							}
							
							// reset the sample counter
							sampleCount = 0;
						}
					}
				}
			}
			
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// write header to the file
		private void writeHeader() throws IOException, BDFException {
			if(bdf.getHeader() != null) {
				outputStream.write(bdf.getHeader().getBytes());
			} else {
				throw new BDFException(String.format("BDFHeader was not set."));
			}
		}
		
		// write data from the current record to the file
		private void writeRecord() throws IOException {
			for(int c = 0; c < numChannels; c++) {
				for(int v = 0; v < sampleRate; v++) {
					
					byte[] bytes = parseValue(record[c][v]);
					
					outputStream.write(bytes);
				}
			}
		}
		
		// covert sample into a 3 byte array
		private byte[] parseValue(int value) {
			byte[] bytes = new byte[3];
			
			bytes[0] = (byte) (value & 0xFF);
			bytes[1] = (byte) ((value >> 8) & 0xFF);
			bytes[2] = (byte) ((value >>> 16) & 0xFF);
			
			return bytes;
		}
	}	
}