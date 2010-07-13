package it.hakvoort.bdf;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class BDFReader {

	private int THRESHOLD_MIN = 2;
	private int THRESHOLD_MAX = 4;
	
	private int frequency = -1;

	// the number of samples per second
	private int sampleRate = 0;
	
	// listeners waiting for samples
	protected List<BDFListener> listeners = new CopyOnWriteArrayList<BDFListener>();
	
	// the pathname of the bdf file
	private String pathname = null;
	
	// the bdf file
	private BDFFile bdf = null;
	
	// the InputStream for reading data
	private InputStream inputStream = null;
	
	// the fileReader, reading the data and puts data in the queue.
	private BDFFileReader fileReader = new BDFFileReader();
	
	// the dataReader, notifying the listeners about new data
	private BDFDataReader dataReader = new BDFDataReader();
		
	// if the file is opened
//	private boolean open = false;

	// if the file and data readers are running
	private boolean running = false;
	
	// should the reader start at the beginning of the file when reaching the end
	private boolean repeat = false;
	
	// samples read from the bdf file
	private ConcurrentLinkedQueue<BDFSample> samples = new ConcurrentLinkedQueue<BDFSample>();
	
	public BDFReader(BDFFile bdf) {
		this.bdf = bdf;
		
		sampleRate = bdf.getSampleRate();
	}
		
	public int getFrequency() {
		if(this.frequency == 0) {
			return this.sampleRate;
		}
		
		return this.frequency;
	}
	
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	
	public int getThresholdMin() {
		return this.THRESHOLD_MIN;
	}
	
	public void setThresholdMin(int minValue) {
		this.THRESHOLD_MIN = minValue;
	}

	public int getThresholdMax() {
		return this.THRESHOLD_MAX;
	}
	
	public void setThresholdMax(int maxValue) {
		this.THRESHOLD_MAX = maxValue;
	}
	
	public boolean repeat() {
		return this.repeat;
	}
	
	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}
	
	public boolean isRunning() {
		return this.running;
	}
	
	/*
	 * Start the BDFReader, start the read and data threads.
	 */
	public void start() {
		fileReader.start();
		dataReader.start();
		
		running = true;
	}
	
	/*
	 * Stop the BDFReader from further processing new data
	 */
	public void stop() {
		running = false;
		
		try {
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public BDFFile getBDFFile() {
		return bdf;
	}
	
	public void addListener(BDFListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(BDFListener listener) {
		listeners.remove(listener);
	}
	
	public void removeAllListeners() {
		listeners.clear();
	}
	
	public List<BDFListener> getListeners() {
		return listeners;
	}
	
	/**
	 * Send a data record sample to all listeners
	 */
	public void fireReceivedDataRecordSample(BDFSample sample) {
		for(BDFListener listener : listeners) {
			listener.receivedSample(sample);
		}
	}
	
	/**
	 * BDFFileReader, reads records from the BDF file and stores it in a temporary buffer.
	 * The buffer will be converted into multiple BDFSamples and all samples are placed in a Queue for further processing.
	 */
	private class BDFFileReader extends Thread {
		
		private int counter = 0;
		
		public BDFFileReader() {
			
		}
		
		public void run() {
			
			// get number of channels
			int numChannels = bdf.getNumChannels();
			
			if(numChannels == 0) {
				System.err.println(String.format("NO BDFChannels were set."));
				running = false;
			}
			
			// get length of header
			int length = Integer.parseInt(bdf.getHeader().getLength());
			
			// two dimensional array with size of sampleRate x numChannels
			int[][] record = new int[numChannels][];
			
			try {
				inputStream = new FileInputStream(bdf.getFile());
				inputStream.skip(length);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// get all samples within a record
			// records are store as a byte array, one after another. In 1 records there can be multiple channels.
			//
			// example below has 2 records, with 2 channels and a sampleRate of 3 samples per second.
			//
			// file 	[								 bdf file								]
			// records	[              record1              |              record2              ]
			// channels [    channel1     |    channel2     |    channel1     |    channel2     ]
			// values	[ v11 | v12 | v13 | v21 | v22 | v23 | v14 | v15 | v16 | v24 | v25 | v26 ]
			// bytes	[1 2 3|1 2 3|1 2 3|1 2 3|1 2 3|1 2 3|1 2 3|1 2 3|1 2 3|1 2 3|1 2 3|1 2 3]
			while(running) {
				try {
					
					// a buffer which can contain 1 channel of a record
					byte[] data = new byte[sampleRate * 3];
					
					// read channel for channel of a record
					for(int i = 0; i < numChannels; i++) {
						inputStream.read(data);
						record[i] = parseChannel(data);
					}
					
					// create BDFSamples which contain the sample value for each channel.
					// for the previous example this will eventually results in 6 samples, each with 2 values.
					// 
					// 			   values
					// 	 -- record 1 --
					// sample1	[ v11 | v21 ]
					// sample2	[ v12 | v22 ]
					// sample3  [ v13 | v23 ]
					//   -- record 2 --
					// sample4  [ v14 | v24 ]
					// sample5  [ v15 | v26 ]
					// sample6  [ v16 | v25 ]
					for(int v = 0; v < sampleRate; v++) {

						// new array for all values within the sample
						int[] values = new int[numChannels];
						
						for(int c = 0; c < numChannels; c++) {
							values[c] = record[c][v];
						}
						
						samples.add(new BDFSample(counter++, values));
					}

					// check if the file is running out of data, restart at the begin of the file
					if(inputStream.available() <= 0) {
						if(repeat) {
							inputStream = new FileInputStream(bdf.getFile());
							inputStream.skip(length);
							
							// reset the counter
							counter = 0;
						} else {
							running = false;
							break;
						}
					}
					
					if(samples.size() >= THRESHOLD_MAX * sampleRate) {
						synchronized(this) {
							this.wait();
						}
					}
					
				} catch(IOException e) {
					running = false;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			running = false;
		}
		
		private int[] parseChannel(byte[] data) {		
			ByteBuffer buffer = ByteBuffer.wrap(data);

			int[] values = new int[data.length / 3];
			
			int counter = 0;
			
			byte[] bytes = new byte[3];
			
			while(buffer.remaining() > 0) {
				buffer.get(bytes);
				values[counter++] = parseValue(bytes);
			}
			
			return values;
		}
		
		private int parseValue(byte[] bytes) {
			int value = (bytes[0] & 0xFF) | ((bytes[1] & 0xFF) << 8) | ((bytes[2] & 0xFF) << 16);
			value = ~(value ^ 0x7fffff) + 0x800000;
			return value;
		}
	}
	
	/**
	 * BDFDataReader, fetches the first BDFDataRecordSample from the queue and sends it to all listeners.
	 */
	private class BDFDataReader extends Thread {

		public void run() {
			while(running || !samples.isEmpty()) {
				
				BDFSample recordSample = samples.poll();
				
				if(recordSample != null) {
					fireReceivedDataRecordSample(recordSample);
				}
				
				if(samples.size() < THRESHOLD_MIN * sampleRate) {
					synchronized(fileReader) {
						fileReader.notify();
					}
				}
				
				try {
					if(frequency == -1) {
						sleep(Math.round(1000l / (double) sampleRate));
					} else if(frequency > 0) {
						sleep(Math.round(1000l / (double) frequency));
					} else {
						// full speed
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}