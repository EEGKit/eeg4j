package it.hakvoort.nia2tcp;

import it.hakvoort.nia.NiaDevice;
import it.hakvoort.nia.NiaListener;
import it.hakvoort.nia.NiaSample;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class NiaNetworkServer implements Runnable {
	
	// the server socket
	private ServerSocket serverSocket;
	
	// the nia
	private NiaDevice device;
	
	// the hostname or ip address of the server
	private String HOST;
	
	// the server port
	private int PORT;
	
	// if the server is listening for new connections
	private boolean listening = true;
	
	// the number of connected clients
	private AtomicInteger connectedClients = new AtomicInteger(0);
	
	// the server thread
	private Thread serverThread;

	public NiaNetworkServer(NiaDevice device, String HOST, int PORT) {
		this.device = device;
		
		this.HOST = HOST;
		this.PORT = PORT;
	}
	
	public synchronized void start() {
		if(!device.isConnected()) {
			device.start();
		}
		
		if(this.serverThread == null) {
			this.serverThread = new Thread(this);
			this.serverThread.start();
		}
	}
	
	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(HOST, PORT));
			
			System.out.println(String.format("NiaServer ready and listening for connections on: %s:%s.", HOST, PORT));
			
			while(listening) {
				Socket socket = serverSocket.accept();
				
				NiaClientHandler handler = new NiaClientHandler(socket);
				device.addListener(handler);
				
				synchronized(connectedClients) {
					connectedClients.incrementAndGet();
				}
				
				String name = String.format("NiaClient_%s", connectedClients);
				
				System.out.println(String.format("%s connected from '%s'.", name, socket.getInetAddress().getHostAddress()));
				
				Thread handlerThread = new Thread(handler);
				handlerThread.setName(name);
				handlerThread.setDaemon(true);
				handlerThread.start();
			}
			
		} catch(IOException e) {
			e.printStackTrace();
			System.err.println(String.format("Could not bind socket to address %s:%s", HOST, PORT));
		}
	}
	
	public synchronized void stop() {
		listening = false;
		
		try {
			serverSocket.close();
		} catch (IOException e) {
			System.err.println(String.format("Disconnect error"));
		}
		
		if(device.isConnected()) {
			device.stop();
		}
	}

	public int getConnectedClients() {
		return connectedClients.get();
	}
	
	public class NiaClientHandler implements Runnable, NiaListener {
		
		// the input stream to keep the connection open
		private InputStream in;
		
		// the output stream to send data to
		private OutputStream out;
		
		// if the handler is connected
		private boolean connected = true; 
		
		public NiaClientHandler(Socket socket) throws IOException {
			in = socket.getInputStream();
			out = socket.getOutputStream();
		}
		
		@Override
		public void run() {
			try {
				while(connected && in.read() != -1) {}
			} catch (IOException e) {
				connected = false;
			}
			
			synchronized(connectedClients) {
				connectedClients.decrementAndGet();
			}
			
			System.out.println(String.format("%s disconnected.", Thread.currentThread().getName()));
		}
		
		@Override
		public void receivedSample(NiaSample sample) {
			byte[] data = new byte[3];
				
			data[0] = (byte) (sample.value & 0xFF);
			data[1] = (byte) ((sample.value >> 8) & 0xFF);
			data[2] = (byte) ((sample.value >>> 16) & 0xFF);
			
			try {
				out.write(data);
			} catch (IOException e) {
				connected = false;
			}
		}
	}
	
	public static void main(String[] args) {
		NiaDevice device = new NiaDevice();
		NiaNetworkServer niaBDFLink = new NiaNetworkServer(device, "localhost", 4321);
		
		
	}
}
