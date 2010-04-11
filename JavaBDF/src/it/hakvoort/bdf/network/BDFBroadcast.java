package it.hakvoort.bdf.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * BDFBroadcast connects to a BDF Server and broadcasts the incomming data to all connected clients.
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class BDFBroadcast {

	// the connected clients
	protected List<BDFBroadcastClientHandler> clients = new CopyOnWriteArrayList<BDFBroadcastClientHandler>();
	
	// the server thread
	private Thread serverThread;

	// the client thread
	private Thread clientThread;
	
	// if the client is connected
	private boolean running = false;
	
	public BDFBroadcast(String HOST, int SERVER_PORT, int numChannels, int BROADCAST_PORT) {

		// create a client for receiving incomming data
		BDFBroadcastClient client = new BDFBroadcastClient(HOST, SERVER_PORT, numChannels);
		
		// create a server for handling new incomming connections
		BDFBroadcastServer server = new BDFBroadcastServer(BROADCAST_PORT);
		
		clientThread = new Thread(client);
		client.connect();
		serverThread = new Thread(server);
		
		clientThread.start();
		serverThread.start();
	}
	
	/**
	 * Send the data to all connected clients
	 */
	private void fireReceivedData(byte[] data) {
		for(BDFBroadcastClientHandler client : clients) {
			client.receivedData(data);
		}
	}
	
	/**
	 * BDFBroadcastClient connects to a BDF Server and sends the incomming data to all connected clients.
	 *
	 */
	private class BDFBroadcastClient implements Runnable {
		
		// the client socket
		private Socket socket;
		
		// the input stream
		private InputStream input;

		// the hostname or ip address of the server
		private String HOST;
		
		// the server port
		private int PORT;
		
		// the number of channels in the stream
		private int numChannels;
		
		public BDFBroadcastClient(String HOST, int PORT, int numChannels) {
			this.HOST = HOST;
			this.PORT = PORT;
			
			this.numChannels = numChannels;
		}
		
		private void connect() {
			try {
				socket = new Socket();
				socket.connect(new InetSocketAddress(HOST, PORT), 5000);
				input = socket.getInputStream();

				running = true;
			} catch (UnknownHostException e) {
				System.err.println(String.format("Unknown Host: %s", HOST));
			} catch (IOException e) {
				System.err.println(String.format("Could not connect to %s:%s", HOST, PORT));
			}
		}
		
		public void run() {
			byte[] buffer = new byte[numChannels * 3];

			try {
				while(running && input.read(buffer) != -1) {
					fireReceivedData(buffer);
				}
			} catch(IOException e) {
				
			}
			
			running = false;
		}
	}
	
	/**
	 * BDFBroadcastServer listens for incomming connections and handles them proparly.
	 */
	private class BDFBroadcastServer implements Runnable {
		
		// the server socket
		private ServerSocket serverSocket;

		// the hostname or ip address of the server
		private String HOST;
		
		// the server port
		private int PORT;
		
		public BDFBroadcastServer(int PORT) {
			this.PORT = PORT;
		}
		
		public void run() {		
			if(!running) {
				return;
			}
			
			try {
				serverSocket = new ServerSocket();
				serverSocket.bind(new InetSocketAddress(PORT));
				
				serverSocket.setSoTimeout(10000);
				
				HOST = InetAddress.getLocalHost().getHostAddress();
				
				System.out.println(String.format("BDFBroadcastServer connected and listening for connections on: %s:%s.", HOST, PORT));
				
			} catch(IOException e) {
				e.printStackTrace();
				System.err.println(String.format("Could not bind socket to address %s:%s", HOST, PORT));
			}
			
			while(running) {
				try {
					Socket socket = serverSocket.accept();
					BDFBroadcastClientHandler handler = new BDFBroadcastClientHandler(socket);
					
					clients.add(handler);
					
					String name = String.format("BDFClient_%s", clients.size());
					
					System.out.println(String.format("%s connected from '%s'.", name, socket.getInetAddress().getHostAddress()));
					
					Thread handlerThread = new Thread(handler);
					handlerThread.setName(name);
					handlerThread.setDaemon(true);
					handlerThread.start();
				} catch(SocketTimeoutException e) {
					// timeout, for checking if the client is still connected
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * BDFBroadcastClientHandler handles unique client connections. The handler receives byte arrays of data from the BDFBroadcastClient and
	 * sending them over the network, mimicing Biosemi's ActiView network connection.
	 */
	public class BDFBroadcastClientHandler implements Runnable {
		
		// the input stream to keep the connection open
		private InputStream in;
		
		// the output stream to send data to
		private OutputStream out;
		
		// if the handler is connected
		private boolean connected = true; 
		
		public BDFBroadcastClientHandler(Socket socket) throws IOException {
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
			
			clients.remove(this);
			System.out.println(String.format("%s disconnected.", Thread.currentThread().getName()));
		}
		
		public void receivedData(byte[] data) {
			try {
				out.write(data);
			} catch (IOException e) {
				connected = false;
			}
		}
	}
	
	public static void main(String[] args) {
		if(args.length < 4) {
			System.out.println("Usage: BDFBroadcast HOSTNAME SERVER_PORT CHANNELS BROADCAST_PORT");
			System.out.println("HOSTNAME       : hostname of the BDF server.");
			System.out.println("SERVER_PORT    : port number of the BDF server.");
			System.out.println("CHANNELS       : the number of channels in the BDF data stream.");
			System.out.println("BROADCAST_PORT : port number for connecting clients.");
			
			return;
		}
		
		String HOST 		= args[0];
		int SERVER_PORT 	= Integer.parseInt(args[1]);
		int CHANNELS 		= Integer.parseInt(args[2]);
		int BROADCAST_PORT 	= Integer.parseInt(args[3]);
		
		new BDFBroadcast(HOST, SERVER_PORT, CHANNELS, BROADCAST_PORT);
	}
}