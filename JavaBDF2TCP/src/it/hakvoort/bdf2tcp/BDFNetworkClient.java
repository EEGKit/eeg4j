package it.hakvoort.bdf2tcp;

import it.hakvoort.bdf.BDFDataRecord;
import it.hakvoort.bdf.BDFListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class BDFNetworkClient {
	
	// the client socket
	private Socket socket;
	
	// the input stream
	private InputStream input;
	
	// the hostname or ip address of the server
	private String HOST;
	
	// the server port
	private int PORT;
	
	// if the client is connected
	private boolean connected = false;

	// the input reader
	private BDFClientInputReader inputReader;
	
	// listeners waiting for records
	protected List<BDFListener> listeners = new CopyOnWriteArrayList<BDFListener>();
	
	public BDFNetworkClient(String HOST, int PORT, int numChannels) {
		this.HOST = HOST;
		this.PORT = PORT;
		
		inputReader = new BDFClientInputReader(numChannels);
	}
	
	public void connect() {
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(HOST, PORT), 5000);
			input = socket.getInputStream();

			connected = true;
		} catch (UnknownHostException e) {
			System.err.println(String.format("Unknown Host: %s", HOST));
		} catch (IOException e) {
			System.err.println(String.format("Could not connect to %s:%s", HOST, PORT));
		}
		
		if(connected) {
			new Thread(inputReader).start();
		}
	}
	
	public void disconnect() {
		if(!connected) {
			return;
		}

		connected = false;

		try {
			input.close();
			socket.close();
		} catch (IOException e) {
			System.err.println(String.format("Disconnect error"));
		}
	}
	
	public boolean isConnected() {
		return this.connected;
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
	protected void fireReceivedRecord(BDFDataRecord record) {
		for(BDFListener listener : listeners) {
			listener.receivedRecord(record);
		}
	}
	
	/**
	 * BDFClientInputReader reads data from the input stream. 
	 * The data is converted into a BDFDataRecord and send to all listeners
	 */
	private class BDFClientInputReader implements Runnable {
		
		// the number of channels in the stream
		private int numChannels;
		private int recordCounter = 0;
		
		public BDFClientInputReader(int numChannels) {
			this.numChannels = numChannels;
		}
		
		public void run() {
			byte[] buffer = new byte[numChannels * 3];
			int[] samples = new int[numChannels];
			
			try {
				while(connected && input.read(buffer) != -1) {
					
					for(int i=0; i<numChannels; i++) {
						int sample = (buffer[i*3] & 0xFF) | ((buffer[i*3+1] & 0xFF) << 8) | ((buffer[i*3+2] & 0xFF) << 16);
						
						if((sample & 0x800000) != 0) {
							sample = ~(sample ^ 0x7fffff) + 0x800000;
						}
						
						samples[i] = sample;
					}
					
					fireReceivedRecord(new BDFDataRecord(recordCounter, samples));
					recordCounter++;						
				}
			} catch(IOException e) {
				
			}
			connected = false;
			System.err.println("BDFClient disconnected.");
		}
	}
}
