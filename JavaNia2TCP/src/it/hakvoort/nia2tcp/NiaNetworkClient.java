package it.hakvoort.nia2tcp;

import it.hakvoort.nia.NiaListener;
import it.hakvoort.nia.NiaSample;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NiaNetworkClient {
	
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
	private NiaClientInputReader inputReader;
	
	// listeners waiting for records
	protected List<NiaListener> listeners = new CopyOnWriteArrayList<NiaListener>();
	
	// if the samples are signed
	private boolean signed = true;
	
	public NiaNetworkClient(String HOST, int PORT) {
		this.HOST = HOST;
		this.PORT = PORT;
		
		inputReader = new NiaClientInputReader();
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
	
	public boolean isSigned() {
		return this.signed;
	}
	
	public void setSigned(boolean signed) {
		this.signed = signed;
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
	
	protected void fireReceivedSample(NiaSample sample) {
		for(NiaListener listener : listeners) {
			listener.receivedSample(sample);
		}
	}
	
	private class NiaClientInputReader implements Runnable {
		
		private int recordCounter = 0;
		
		public NiaClientInputReader() {
			
		}
		
		public void run() {
			byte[] buffer = new byte[3];
			
			try {
				while(connected && input.read(buffer) != -1) {
					
					int value = (buffer[0] & 0xFF) | ((buffer[1] & 0xFF) << 8) | ((buffer[2] & 0xFF) << 16);
					
					if(signed && (value & 0x800000) != 0) {
						value = ~(value ^ 0x7fffff) + 0x800000;
					}
					
					fireReceivedSample(new NiaSample(recordCounter, value));
					recordCounter++;						
				}
			} catch(IOException e) {
				
			}
			connected = false;
			System.err.println("NiaNetworkClient disconnected.");
		}
	}
}
