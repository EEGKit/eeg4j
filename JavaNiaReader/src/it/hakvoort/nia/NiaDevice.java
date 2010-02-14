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
				e.printStackTrace();
				System.err.println("NIA device not found.");
				attempt++;
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}
		
		if(connected) {
			System.out.println(String.format("Connected to NIA device."));

			dataReader.start();
			deviceReader.start();
		} else {
			System.err.println(String.format("Could not connect to NIA device. Stopped after %s attemps", attempt));
		}
	}
	
	/*
	 * Stop the NiaDecive from further processing new data
	 */
	public void stop() {
		if(this.device.isOpen()) {
			try {
				this.device.close();
			} catch(USBException e) {
				System.err.println(String.format("Could not disconnect the NIA device."));
			}
		}
		
		connected = false;
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
		private long timestamp = 0;
		private long time = 0;
		
		public FileWriter fileWriter = null; 
		public BufferedWriter out = null;
		
		public NiaDeviceReader() {
			if(logging) {
				try {
					fileWriter = new FileWriter("out.txt");
					out = new BufferedWriter(fileWriter);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void run() {			
			while(connected) {
				try {
					timestamp = System.currentTimeMillis();
					device.readBulk(NiaDevice.ENDPOINT_IN, buffer, buffer.length, 2000, false);
					time = System.currentTimeMillis()-timestamp;
					
					if(logging) {
						logData();
					}
					
					// get the number of samples
					byte nSamples = buffer[54];

					// fetch the total number of hits, divided over 2 bytes (litle endian)
					int hitcount = ((buffer[53] & 0xFF) << 8) | (buffer[52] & 0xFF);

					// fetch the total number of misses, divided over 2 bytes (litle endian)
					int miscount = ((buffer[51] & 0xFF) << 8) | (buffer[50] & 0xFF);
					
					// fetch the samples. each sample is divided over 3 bytes, litle endian
					for(int i=0; i < nSamples*3; i+=3) {
						int sample = (buffer[i] & 0xFF) | ((buffer[i+1] & 0xFF) << 8) | ((buffer[i+2] & 0xFF) << 16);
						
						// according to the HID information of the device, sample are between -8388608 and 8388607
						// meaning the sample has a sign bit and is in two's complement.
						if(signed && ((sample & 0x800000) != 0)) {
							sample = ~(sample ^ 0x7fffff) + 0x800000;
						}
						
						try {
							samples.add(new NiaSample((i/3), miscount, hitcount, sample, time));
						} catch(IllegalStateException e) {
							System.out.println(String.format("Size of array: %s.", samples.size()));
						}
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
			
			stringBuffer.append(Long.toString(time));
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
			while(connected || !samples.isEmpty()) {
				
				NiaSample sample = samples.poll();
				
				if(sample != null) {
					fireReceivedSample(sample);
				}
			}
		}
	}
}