package it.hakvoort.nia;

import it.hakvoort.usb.UsbDeviceManager;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.usb.UsbConfiguration;
import javax.usb.UsbDevice;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbInterface;
import javax.usb.UsbInterfacePolicy;
import javax.usb.UsbPipe;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class NiaDevice2 {
	
	public static final int ATTEMPTS = 5;
	
	public static final short VENDOR = (short) 0x1234;
	public static final short DEVICE = (short) 0x00;
	
	public static final byte ENDPOINT_IN = (byte) 0x81;
	public static final byte ENDPOINT_OUT = (byte) 0x01;
	
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
	private UsbDevice device;
	
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
	
	// a queue with contains all incomming (and missing) samples
	private ConcurrentLinkedQueue<NiaSample> samples = new ConcurrentLinkedQueue<NiaSample>();
	
	public NiaDevice2() {
		init();
	}
	
	public void init() {
		try {
			UsbDeviceManager manager = UsbDeviceManager.getInstance();
			this.device = manager.getDevice(NiaDevice2.VENDOR, NiaDevice2.DEVICE);
		} catch (UsbException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Start the NiaDecive, first a connection is being made after which the read and data threads are started.
	 */
	public void start() {
		while(!connected && attempt < ATTEMPTS) {

			UsbConfiguration usbConfiguration = this.device.getUsbConfiguration((byte) 1);
			UsbInterface usbInterface = usbConfiguration.getUsbInterface((byte) 0);
			
			try {
				usbInterface.claim(new UsbInterfacePolicy() {
					@Override
					public boolean forceClaim(UsbInterface arg0) {
						return true;
					}
				});
				
				connected = true;
				continue;
			} catch (UsbException e) {
				e.printStackTrace();
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
		connected = false;
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
		private int offset = 0;
		
		private int counter = 0;
		
		private int pMiscount = 0;
		
		private int pHitcount = 0;
				
		public NiaDeviceReader() {

		}
		
		public void run() {
			UsbConfiguration usbConfiguration = device.getUsbConfiguration((byte) 1);
			UsbInterface usbInterface = usbConfiguration.getUsbInterface((byte) 0);
			
			UsbEndpoint endpoint = usbInterface.getUsbEndpoint(NiaDevice2.ENDPOINT_IN);
			UsbPipe usbPipe = endpoint.getUsbPipe();
			
			try {
				usbPipe.open();
			} catch (UsbException e) {
				e.printStackTrace();
			}
			
			boolean insync = false;
						
			while(connected) {
				try {
					usbPipe.syncSubmit(buffer);
					
					// get the number of samples
					byte nSamples = buffer[54];
	
					// fetch the total number of hits, divided over 2 bytes (litle endian)
					int hitcount = ((buffer[53] & 0xFF) << 8) | (buffer[52] & 0xFF);
	
					// fetch the total number of misses, divided over 2 bytes (litle endian)
					int miscount = ((buffer[51] & 0xFF) << 8) | (buffer[50] & 0xFF);
					
					// calculate delta hitcount, if hitcount is smaller than pHitcount, there was an overflow 
					// (use 0x10000 instead of 0xFFFF), you need to take zero in account!
					int dHitcount = (hitcount < pHitcount) ? ((hitcount + 0x10000) - pHitcount) : (hitcount - pHitcount);
					
					// calculate delta miscount, if miscount is smaller than pMiscount, there was an overflow
					// (use 0x10000 instead of 0xFFFF), you need to take zero in account!
					int dMiscount = (miscount < (pMiscount-16)) ? ((miscount + 0x10000) - pMiscount) : (miscount - pMiscount);
					
					// store last miscount
					pMiscount = miscount;
					
					// store last hitcount
					pHitcount = hitcount;
					
					if(nSamples < 16 && dMiscount == 0) {
						insync = true;
					}
					
					// TODO: what if never insync?
					if(!insync) {
						continue;
					}
					
					// update the offset
					offset += dMiscount;
					
					// update cycle counter
					counter += dHitcount;

					// report missing samples
					for(int i=0; i < (offset - (offset%32)); i++) {
						
						// get sample number
						int number = ((counter - offset) - nSamples) + i;
					
						try {
							samples.add(new NiaSample(number, NiaDevice2.MISSING_SAMPLE_VALUE));
							
							synchronized(samples) {
								samples.notifyAll();
							}
						} catch(IllegalStateException e) {
							
						}	
					}
					
					// update offset, don't fall to far behind
					offset %= INTERNAL_BUFFER_SIZE;
					
					// fetch the samples. each sample is divided over 3 bytes, litle endian
					for(int i=0; i < nSamples; i++) {
						
						// get sample number
						int number = ((counter - offset) - nSamples) + i;
						
						// get sample value
						int value = (buffer[i*3] & 0xFF) | ((buffer[i*3+1] & 0xFF) << 8) | ((buffer[i*3+2] & 0xFF) << 16);
						
						// according to the HID information of the device, sample are between -8388608 and 8388607
						// meaning the sample has a sign bit and is in two's complement.
						if(signed && ((value & 0x800000) != 0)) {
							value = ~(value ^ 0x7fffff) + 0x800000;
						}
						
						try {
							samples.add(new NiaSample(number, value));
	
							synchronized(samples) {
								samples.notifyAll();
							}
						} catch(IllegalStateException e) {
							
						}
					}
				} catch(UsbException e) {
					e.printStackTrace();
				}
			}
			
			try {
				usbPipe.close();
				usbPipe.getUsbEndpoint().getUsbInterface().release();
			} catch(UsbException e) {
				e.printStackTrace();
			}
	
			System.out.println(String.format("NiaDeviceReader: No longer connected, stopped reading data from the device."));
		}
	}
	
	/**
	 * NiaDataReader, fetches the first NiaSample from the queue and sends it to all listeners.
	 */
	private class NiaDataReader extends Thread {

		private double interval = 0;
		private double nextInterval = 0;
		
		public void run() {
			
			interval = (double) SAMPLE_RATE / (double) sampleRate;
			
			while(connected) {
				synchronized(samples) {
					while(samples.isEmpty()) {
						try {
							samples.wait();
						} catch(InterruptedException e) {
							
						}
					}
					
					while(!samples.isEmpty()) {
						NiaSample sample = samples.poll();
						
						if(sample.number >= nextInterval) {
							fireReceivedSample(sample);
							nextInterval += interval;
						}
					}
				}		
			}
		}
	}
}
