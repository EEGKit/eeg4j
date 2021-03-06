package it.hakvoort.bdf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
	
	// the filename
	private String file = null;
		
	// the bdf file inputstream
	private FileInputStream inputStream;
	
	// the fileReader, reading the data and puts data in the queue.
	private BDFFileReader fileReader = new BDFFileReader();
	
	// the dataReader, notifying the listeners about new data
	private BDFDataReader dataReader = new BDFDataReader();
		
	// if the file is open
	private boolean open = false;

	// if the file and data readers are running
	private boolean running = false;
	
	// should the reader start at the beginning of the file when reaching the end
	private boolean repeat = false;
	
	// the header which is fetched from the bdf file
	private BDFHeader header;

	// records read from the bdf file
	private ConcurrentLinkedQueue<BDFDataRecord> records = new ConcurrentLinkedQueue<BDFDataRecord>();
	
	public BDFReader(String location) {
		this.file = location;
		open();
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
	
	/*
	 * Open the BDF file and read the header
	 */
	private boolean open() {
		if(!open) {
			try {
				inputStream = new FileInputStream(file);
				open = true;
				
				// get the file header
				header = new BDFHeader();
				
				byte[] main = new byte[256];				
				inputStream.read(main);
				header.setMainHeader(main);
				
				byte[] signals = new byte[256*header.getNumChannels()];
				inputStream.read(signals);
				header.setChannelHeader(signals);
				
				sampleRate = header.channels.get(0).getNumSamples();
			} catch (FileNotFoundException e) {
				System.err.println(String.format("BDFReader: BFD file not found: '%s'", file));
			} catch (IOException e) {
				System.err.println(String.format("BDFReader: Error during reading BDF header, file closed."));
				open = false;
			}
		}
		
		return open;
	}
	
	public boolean isOpen() {
		return this.open;
	}
	
	public boolean isRunning() {
		return this.running;
	}
	
	/*
	 * Start the BDFReader, start the read and data threads.
	 */
	public void start() {
		if(open) {
			fileReader.start();
			dataReader.start();
		} else {
			System.err.println(String.format("BDFReader: BDF file: '%s' is closed", file));
		}
		
		running = true;
	}
	
	/*
	 * Stop the BDFReader from further processing new data
	 */
	public void stop() {
		running = false;
		open = false;
		records.clear();
		
		try {
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public BDFHeader getHeader() {
		return header;
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
	 * Send a sample to all listeners
	 */
	public void fireReceivedSample(BDFDataRecord sample) {
		for(BDFListener listener : listeners) {
			listener.receivedRecord(sample);
		}
	}
	
	/**
	 * BDFFileReader, reads data from the BDF file and stores it in a temporary buffer.
	 * The buffer will be converted into multiple BDFSamples and all samples are placed in a Queue for further processing.
	 */
	private class BDFFileReader extends Thread {
		
		private int counter = 0;
		
		public BDFFileReader() {
			
		}
		
		public void run() {
			int[][] record = new int[header.getNumChannels()][];
			
			// get all records
			while(open) {
				try {
					byte[] data = new byte[sampleRate * 3];
					
					for(int i = 0; i<header.getNumChannels(); i++) {
						inputStream.read(data);
						record[i] = parseChannel(data);
					}
					
					// fill the queue with the data just read
					for(int s = 0; s < sampleRate; s++) {
						int[] samples = new int[header.getNumChannels()];
						
						for(int c = 0; c < header.getNumChannels(); c++) {
							samples[c] = record[c][s];
						}
						
						records.add(new BDFDataRecord(counter++, header.getNumChannels(), samples));
					}

					// check if the file is running out of data
					if(inputStream.available() <= 0) {
						if(repeat) {
							inputStream = new FileInputStream(file);
							inputStream.skip(header.getLength());
							
							// reset the counter
							counter = 0;
						} else {
							open = false;
							break;
						}
					}
					
					if(records.size() >= THRESHOLD_MAX * sampleRate) {
						synchronized(this) {
							this.wait();
						}
					}
					
				} catch(IOException e) {
					open = false;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			open = false;
		}
		
		private int[] parseChannel(byte[] data) {		
			ByteBuffer buffer = ByteBuffer.wrap(data);

			int[] values = new int[data.length / 3];
			
			int counter = 0;
			
			byte[] sample = new byte[3];
			
			while(buffer.remaining() > 0) {
				buffer.get(sample);
				values[counter++] = parseSample(sample);
			}
			
			return values;
		}
		
		private int parseSample(byte[] bytes) {
			int sample = (bytes[0] & 0xFF) | ((bytes[1] & 0xFF) << 8) | ((bytes[2] & 0xFF) << 16);
			sample = ~(sample ^ 0x7fffff) + 0x800000;
			return sample;
		}
	}
	
	/**
	 * BDFDataReader, fetches the first BDFRecord from the queue and sends it to all listeners.
	 */
	private class BDFDataReader extends Thread {

		public void run() {
			while(open || !records.isEmpty()) {
				
				BDFDataRecord record = records.poll();
				
				if(record != null) {
					fireReceivedSample(record);
				}
				
				if(records.size() < THRESHOLD_MIN * sampleRate) {
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