package it.hakvoort.edf.network;

import it.hakvoort.edf.EDFListener;
import it.hakvoort.edf.EDFSample;

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
public class EDFClient {
	
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
	private EDFClientInputReader inputReader;
	
	// listeners waiting for records
	protected List<EDFListener> listeners = new CopyOnWriteArrayList<EDFListener>();
	
	public EDFClient(String HOST, int PORT, int numChannels) {
		this.HOST = HOST;
		this.PORT = PORT;
		
		inputReader = new EDFClientInputReader(numChannels);
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
	
	public void addListener(EDFListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(EDFListener listener) {
		listeners.remove(listener);
	}
	
	public void removeAllListeners() {
		listeners.clear();
	}
	
	public List<EDFListener> getListeners() {
		return listeners;
	}
	
	/**
	 * Send a sample to all listeners
	 */
	protected void fireReceivedRecord(EDFSample sample) {
		for(EDFListener listener : listeners) {
			listener.receivedSample(sample);
		}
	}
	
	/**
	 * EDFClientInputReader reads data from the input stream. 
	 * The data is converted into a EDFSample and send to all listeners
	 */
	private class EDFClientInputReader implements Runnable {
		
		// the number of channels in the stream
		private int numChannels;
		private int recordCounter = 0;
		
		public EDFClientInputReader(int numChannels) {
			this.numChannels = numChannels;
		}
		
		public void run() {
			byte[] buffer = new byte[numChannels * 2];
			int[] samples = new int[numChannels];
			
			int index = 0;
			
			try {
				while(connected) {
					// get next byte
					int b = input.read();
					
					// stop if end of stream
					if(b == -1) {
						connected = false;
						continue;
					}
					
					// store byte in buffer
					buffer[index] = (byte) b;
					
					// update index
					index++;
					
					// if buffer is not full, continue reading data
					if(index < buffer.length) {
						continue;
					}
								
					// process data and send record
					for(int i=0; i<numChannels; i++) {
						int value = (buffer[i*2] & 0xFF) | ((buffer[i*2+1] & 0xFF) << 8);
					
						// TODO: is it signed?
						if((value & 0x8000) != 0) {
							value = ~(value ^ 0x7fff) + 0x8000;
						}
					
						samples[i] = value;
					}
					
					fireReceivedRecord(new EDFSample(recordCounter, samples));
					recordCounter++;						
					
					// reset samples
					samples = new int[numChannels];
					
					// reset index
					index = 0;
				}
			} catch(IOException e) {
				
			}
			
			connected = false;
			System.err.println("EDFClient disconnected.");
		}
	}

	public static void main(String[] args) {
		if(args.length < 3) {
			System.out.println("Usage: EDFClient HOSTNAME PORT CHANNELS");
			System.out.println("HOSTNAME : hostname of the EDF server.");
			System.out.println("PORT     : port number of the EDF server.");
			System.out.println("CHANNELS : the number of channels in the EDF data stream.");
			
			return;
		}
		
		String HOST 	= args[0];
		int PORT 		= Integer.parseInt(args[1]);
		int CHANNELS 	= Integer.parseInt(args[2]);
		
		EDFClient client = new EDFClient(HOST, PORT, CHANNELS);
		client.addListener(new EDFListener() {
			
			@Override
			public void receivedSample(EDFSample sample) {
				System.out.println(sample.toString());
			}
		});
		
		client.connect();
	}
}