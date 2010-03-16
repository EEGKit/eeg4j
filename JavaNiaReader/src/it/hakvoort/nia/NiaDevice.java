package it.hakvoort.nia;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import ch.ntb.usb.Device;
import ch.ntb.usb.USB;
import ch.ntb.usb.USBException;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class NiaDevice {
	
	public static final int ATTEMPTS = 5;
	
	public static final short VENDOR = (short) 0x1234;
	public static final short DEVICE = (short) 0x00;
	
	public static final int ENDPOINT_IN = 0x81;
	public static final int ENDPOINT_OUT = 0x01;
	
	// the size of Nia's internal buffer (this is the maximum number of sample Nia gets behind)
	public static final int INTERNAL_BUFFER_SIZE = 32;
	
	// internal sample rate of Nia in Hz
	public static final int SAMPLE_RATE = 3906;
	
	// the value to give missing sample
	public static final int MISSING_SAMPLE_VALUE = Integer.MIN_VALUE;
	
	// prefered sample rate of the NiaDevice.
	// every SAMPLE_RATE/sampleRate clock cycles a new sample is read and send to all listeners
	private int sampleRate = 512;
	
	// listeners waiting for samples
	protected List<NiaListener> listeners = new CopyOnWriteArrayList<NiaListener>();
	
	// the ocz nia usb device
	private Device device;
	
	// the deviceReader, reading the data and puts data in the queue.
	private NiaDeviceReader deviceReader = new NiaDeviceReader();
	
	// the dataReader, notifying the listeners about new data
	private NiaDataReader dataReader = new NiaDataReader();
	
	// if incoming data should be written to a file
	private boolean logging = false;
	
	// if the device is connected
	private boolean connected = false;
	
	// if the samples are signed
	private boolean signed = true;
	
	// the number of connection attempts
	private int attempt = 0;
	
	private ConcurrentLinkedQueue<NiaSample> samples = new ConcurrentLinkedQueue<NiaSample>();
	
	public NiaDevice() {
		init();
	}
	
	public void init() {
		this.device = USB.getDevice(NiaDevice.VENDOR, NiaDevice.DEVICE);
	}
	
	/*
	 * Start the NiaDecive, first a connection is being made after which the read and data threads are started.
	 */
	public void start() {
		while(!connected && attempt < ATTEMPTS) {
			try {
				this.device.open(1, 0, -1);
				
				connected = true;
				continue;
			} catch (USBException e) {
				System.err.println("NIA device not found.");
				attempt++;
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}
		
		if(connected) {
			System.out.println(String.format("Connected to NIA device."));

			deviceReader.start();
			deviceReader.setPriority(Thread.MAX_PRIORITY);
			dataReader.start();
		} else {
			System.err.println(String.format("Could not connect to NIA device. Stopped after %s attemps", attempt));
		}
	}
	
	/*
	 * Stop the NiaDecive from further processing new data
	 */
	public void stop() {
		connected = false;
		
		if(this.device.isOpen()) {
			try {
				this.device.close();
			} catch(USBException e) {
				System.err.println(String.format("Could not disconnect the NIA device."));
			}
		}		
	}
	
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}
	
	public int getSampleRate() {
		return this.sampleRate;
	}
	
	public boolean isConnected() {
		return this.connected;
	}
	
	public boolean getSigned() {
		return signed;
	}
	
	public void setSigned(boolean signed) {
		this.signed = signed;
	}
	
	public boolean getLogging() {
		return logging;
	}
	
	public void setLogging(boolean logging) {
		this.logging = logging;
	}
	
	public void addListener(NiaListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(NiaListener listener) {
		listeners.remove(listener);
	}
	
	public void removeAllListeners() {
		listeners.clear();
	}
	
	public List<NiaListener> getListeners() {
		return listeners;
	}
	
	/**
	 * Send a sample to all listeners
	 */
	public void fireReceivedSample(NiaSample sample) {
		for(NiaListener listener : listeners) {
			listener.receivedSample(sample);
		}
	}
	
	/**
	 * NiaDeviceReader, reads data from the OCZ NIA and stores it in a temporary buffer.
	 * The buffer will be converted into multiple NiaSamples and all samples are placed in a Queue for further processing.
	 */
	private class NiaDeviceReader extends Thread {
		
		private byte buffer[] = new byte[55];
		
		public FileWriter fileWriter = null; 
		public BufferedWriter out = null;
		
		public NiaDeviceReader() {

		}
		
		public void run() {
			if(logging) {
				try {
					fileWriter = new FileWriter("out.txt");
					out = new BufferedWriter(fileWriter);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			long start = System.nanoTime();
			
			long previousSampleTime = 0;
			
			long nanoPerSample = 1000000000l / sampleRate;
			
			int sendSamples = 0;

			long prevTime = start;

			while(connected) {
				try {
					long beforeReadTime = System.nanoTime() - start;
					device.readInterrupt(NiaDevice.ENDPOINT_IN, buffer, buffer.length, 2000, false);
					long afterReadTime = System.nanoTime() - start;
					
					if(logging) {
						logData();
					}
					
					// get the number of samples
					byte nSamples = buffer[54];

					// fetch the total number of hits, divided over 2 bytes (litle endian)
					int hitcount = ((buffer[53] & 0xFF) << 8) | (buffer[52] & 0xFF);

					// fetch the total number of misses, divided over 2 bytes (litle endian)
					int miscount = ((buffer[51] & 0xFF) << 8) | (buffer[50] & 0xFF);
										
					long readDuration = afterReadTime - beforeReadTime;

					// average length of a sample in ns
					long sampleDuration = readDuration / nSamples;
					
					// fetch the samples. each sample is divided over 3 bytes, litle endian
					for(int i=0; i < nSamples; i++) {
						
						// get sample value
						int value = (buffer[i*3] & 0xFF) | ((buffer[i*3+1] & 0xFF) << 8) | ((buffer[i*3+2] & 0xFF) << 16);
						
						// according to the HID information of the device, sample are between -8388608 and 8388607
						// meaning the sample has a sign bit and is in two's complement.
						if(signed && ((value & 0x800000) != 0)) {
							value = ~(value ^ 0x7fffff) + 0x800000;
						}
						
						long sampleTime = beforeReadTime + sampleDuration * i;
						
						if(!(previousSampleTime + nanoPerSample <= sampleTime)) {
							continue;
						}

						sampleTime = (sampleTime / nanoPerSample) * nanoPerSample;

						long sampleDiff = sampleTime - previousSampleTime;

						while(sampleDiff / nanoPerSample > 1) {
							try {
								samples.add(new NiaSample(sendSamples++, NiaDevice.MISSING_SAMPLE_VALUE));
								synchronized (samples) {
									samples.notifyAll();
								}
							} catch (IllegalStateException e) {
							}
							
							// insert empty sample
							sampleDiff -= nanoPerSample;
						}
						
						try {
							samples.add(new NiaSample(sendSamples++, value));
							synchronized (samples) {
								samples.notifyAll();
							}
						} catch (IllegalStateException e) {
						}
						
						previousSampleTime = sampleTime;
					}			
				} catch(USBException e) {
					System.err.println(e.getMessage());
					connected = false;
				}
			}
			
			if(logging) {
				try {
					out.close();
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			System.out.println(String.format("NiaDeviceReader: No longer connected, stopped reading data from the device."));
		}
		
		public void logData() {
			int b = 0;
			String s = "";
			
			StringBuffer stringBuffer = new StringBuffer();
			
			for(int i= 0; i < 55; i++) {
				b = ((int)buffer[i] & 0xff);
			
				s = Integer.toString(b, 16);
				if(s.length() == 1) {
					s = "0"+s;
				}
				
				stringBuffer.append(s);
				stringBuffer.append(" ");
			}
			
			stringBuffer.append("\n");
			
			try {
				out.write(stringBuffer.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * NiaDataReader, fetches the first NiaSample from the queue and sends it to all listeners.
	 */
	private class NiaDataReader extends Thread {
		
		public void run() {
			while(connected) {
				synchronized(samples) {
					while(samples.isEmpty()) {
						try {
							samples.wait();
						} catch(InterruptedException e) {
							
						}
					}
				}
				
				while(!samples.isEmpty()) {
					fireReceivedSample(samples.poll());
				}
			}
		}
	}
}