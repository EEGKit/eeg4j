package it.hakvoort.bdf.network;

import it.hakvoort.bdf.BDFSample;
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
public class BDFClient {
	
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
	
	public BDFClient(String HOST, int PORT, int numChannels) {
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
	protected void fireReceivedRecord(BDFSample sample) {
		for(BDFListener listener : listeners) {
			listener.receivedSample(sample);
		}
	}
	
	/**
	 * BDFClientInputReader reads data from the input stream. 
	 * The data is converted into a BDFSample and send to all listeners
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
			
			int index = 0;
			
			try {
				while(connected) {
					int toRead = buffer.length;
					
					// wait until buffer is full
					while(toRead > 0)  {
						toRead -= input.read(buffer, buffer.length - toRead, toRead);
					}
													
					// process data and send record
					for(int i=0; i<numChannels; i++) {
						int value = (buffer[i*3] & 0xFF) | ((buffer[i*3+1] & 0xFF) << 8) | ((buffer[i*3+2] & 0xFF) << 16);
					
						if((value & 0x800000) != 0) {
							value = ~(value ^ 0x7fffff) + 0x800000;
						}
					
						samples[i] = value;
					}
					
					fireReceivedRecord(new BDFSample(recordCounter, samples));
					recordCounter++;						
					
					// reset index
					index = 0;
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
			
			connected = false;
			System.err.println("BDFClient disconnected.");
		}
	}

	public static void main(String[] args) {
		if(args.length < 3) {
			System.out.println("Usage: BDFClient HOSTNAME PORT CHANNELS");
			System.out.println("HOSTNAME : hostname of the BDF server.");
			System.out.println("PORT     : port number of the BDF server.");
			System.out.println("CHANNELS : the number of channels in the BDF data stream.");
			
			return;
		}
		
		String HOST 	= args[0];
		int PORT 		= Integer.parseInt(args[1]);
		int CHANNELS 	= Integer.parseInt(args[2]);
		
		BDFClient client = new BDFClient(HOST, PORT, CHANNELS);
		client.addListener(new BDFListener() {
			
			@Override
			public void receivedSample(BDFSample sample) {
				System.out.println(sample.toString());
			}
		});
		
		client.connect();
	}
}