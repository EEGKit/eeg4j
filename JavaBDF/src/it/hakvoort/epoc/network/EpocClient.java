package it.hakvoort.epoc.network;

import it.hakvoort.bdf.BDFSample;
import it.hakvoort.bdf.BDFListener;
import it.hakvoort.epoc.EpocListener;
import it.hakvoort.epoc.EpocSample;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class EpocClient {
	
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
	private EpocClientInputReader inputReader;
	
	// listeners waiting for records
	protected List<EpocListener> listeners = new CopyOnWriteArrayList<EpocListener>();
	
	public EpocClient(String HOST, int PORT, int numChannels) {
		this.HOST = HOST;
		this.PORT = PORT;
		
		inputReader = new EpocClientInputReader(numChannels);
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
	
	public void addListener(EpocListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(EpocListener listener) {
		listeners.remove(listener);
	}
	
	public void removeAllListeners() {
		listeners.clear();
	}
	
	public List<EpocListener> getListeners() {
		return listeners;
	}
	
	/**
	 * Send a sample to all listeners
	 */
	protected void fireReceivedRecord(EpocSample sample) {
		for(EpocListener listener : listeners) {
			listener.receivedSample(sample);
		}
	}
	
	/**
	 * EpocClientInputReader reads data from the input stream. 
	 * The data is converted into a EpocSample and send to all listeners
	 */
	private class EpocClientInputReader implements Runnable {
		
		// the number of channels in the stream
		private int numChannels;
		private int recordCounter = 0;
		
		public EpocClientInputReader(int numChannels) {
			this.numChannels = numChannels;
		}
		
		public void run() {
			byte[] buffer = new byte[numChannels * 8];

			ByteBuffer bytes = ByteBuffer.wrap(buffer);
			bytes.order(ByteOrder.LITTLE_ENDIAN);

			double[] samples = new double[numChannels];
			
			try {
				while(connected) {
					int toRead = buffer.length;
					
					// wait until buffer is full
					while(toRead > 0)  {
						toRead -= input.read(buffer, buffer.length - toRead, toRead);
					}
					
					// rewind byte buffer
					bytes.rewind();
					
					// get samples from byte buffer
					for(int i=0; i<numChannels; i++) {
						samples[i] = bytes.getDouble();
					}
					
					// create and fire new sample
					fireReceivedRecord(new EpocSample(recordCounter, samples));
					recordCounter++;						
				}
			} catch(IOException e) {
				
			}
			
			connected = false;
			System.err.println("BDFClient disconnected.");
		}
	}

	public static void main(String[] args) {
		if(args.length < 3) {
			System.out.println("Usage: EpocClient HOSTNAME PORT CHANNELS");
			System.out.println("HOSTNAME : hostname of the Epoc TCP server.");
			System.out.println("PORT     : port number of the Epoc TCP server.");
			System.out.println("CHANNELS : the number of channels in the Epoc data stream.");
			
			return;
		}
		
		String HOST 	= args[0];
		int PORT 		= Integer.parseInt(args[1]);
		int CHANNELS 	= Integer.parseInt(args[2]);
		
		EpocClient client = new EpocClient(HOST, PORT, CHANNELS);
		client.addListener(new EpocListener() {
			
			double prevNumber = 0;
			
			@Override
			public void receivedSample(EpocSample sample) {
				System.out.println(sample.toString());
				
				prevNumber = sample.values[0];
			}
		});
		
		client.connect();
	}
}