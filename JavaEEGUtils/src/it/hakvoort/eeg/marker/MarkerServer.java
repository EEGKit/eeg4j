package it.hakvoort.eeg.marker;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class MarkerServer implements Runnable {

	// the server socket
	private ServerSocket serverSocket;
	
	// the server port
	private int PORT;
	
	// if the server is listening for new connections
	private boolean listening = false;
	
	// the number of connected clients
	private AtomicInteger connectedClients = new AtomicInteger(0);
	
	// the server thread
	private Thread serverThread;

	// listeners waiting for markers
	protected List<MarkerListener> listeners = new CopyOnWriteArrayList<MarkerListener>();
	
	public MarkerServer(int PORT) {
		this.PORT = PORT;
	}
	
	public synchronized void start() {
		listening = true;
		
		if(this.serverThread == null) {
			this.serverThread = new Thread(this);
			this.serverThread.start();
		}
	}
	
	public synchronized void stop() {
		listening = false;
		
		try {
			serverSocket.close();
		} catch (IOException e) {
			System.err.println(String.format("Disconnect error"));
		}		
	}

	public int getConnectedClients() {
		return connectedClients.get();
	}
	
	public void addListener(MarkerListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(MarkerListener listener) {
		listeners.remove(listener);
	}
	
	public void removeAllListeners() {
		listeners.clear();
	}
	
	public List<MarkerListener> getListeners() {
		return listeners;
	}
	
	/**
	 * Send the incomming marker to all listeners
	 */
	protected void fireReceivedMarker(short marker) {
		for(MarkerListener listener : listeners) {
			listener.receivedMarker(marker);
		}
	}	
	
	public void run() {
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(PORT));
			
			while(listening) {
				Socket socket = serverSocket.accept();
				
				MarkerClientHandler handler = new MarkerClientHandler(socket);
				
				synchronized(connectedClients) {
					connectedClients.incrementAndGet();
				}
				
				Thread handlerThread = new Thread(handler);
				handlerThread.setDaemon(true);
				handlerThread.start();
			}
			
		} catch(IOException e) {}
	}
	
	public class MarkerClientHandler implements Runnable {
		
		// the input stream to receive markers
		private DataInputStream in;
		
		// if the handler is connected
		private boolean connected = true; 
		
		public MarkerClientHandler(Socket socket) throws IOException {
			in = new DataInputStream(socket.getInputStream());
		}
		
		@Override
		public void run() {
			short marker;
			
			try {
				while(connected && (marker = in.readShort()) != -1) {
					fireReceivedMarker(marker);
				}
			} catch (IOException e) {
				connected = false;
			}
			
			synchronized(connectedClients) {
				connectedClients.decrementAndGet();
			}
		}
	}
}
