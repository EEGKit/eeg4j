package it.hakvoort.bdf.network;

import it.hakvoort.bdf.BDFException;
import it.hakvoort.bdf.BDFFile;
import it.hakvoort.bdf.BDFSample;
import it.hakvoort.bdf.BDFListener;
import it.hakvoort.bdf.BDFReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class BDFServer implements Runnable {
	
	// the server socket
	private ServerSocket serverSocket;
	
	// the BDFReader
	private BDFReader reader;
	
	// the hostname or ip address of the server
	private String HOST;
	
	// the server port
	private int PORT;
	
	// if the server is listening for new connections
	private boolean listening = false;
	
	// the number of connected clients
	private AtomicInteger connectedClients = new AtomicInteger(0);
	
	// the server thread
	private Thread serverThread;
	
	public BDFServer(BDFFile bdf, int PORT) {
		this(bdf.getReader(), PORT);
	}

	public BDFServer(BDFReader reader, int PORT) {
		this.reader = reader;
		
		this.PORT = PORT;
	}
	
	public synchronized void start() {
		listening = true;
		
		if(this.serverThread == null) {
			this.serverThread = new Thread(this);
			this.serverThread.start();
		}
	}
	
	@Override
	public void run() {
		// if the BDFReader is not running, start the reader
		if(!reader.isRunning()) {
			reader.start();
		}
		
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(PORT));
			
			HOST = InetAddress.getLocalHost().getHostAddress();
			
			// get external ip4address
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			
			while(interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = interfaces.nextElement();
				
				// localhost is loopback
				if(networkInterface.isLoopback()) {
					continue;
				}
				
				Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
				
				while(inetAddresses.hasMoreElements()) {
					
					InetAddress address = inetAddresses.nextElement();
					
					if(address instanceof Inet4Address) {
						HOST = address.getHostAddress();
					}
				}
			}
			
			System.out.println(String.format("BDFServer ready and listening for connections on: %s:%s.", HOST, PORT));
			System.out.println(String.format("Number of channels in TCP stream: %s ", reader.getBDFFile().getHeader().getNumChannels()));
			
			while(listening) {
				Socket socket = serverSocket.accept();
				
				BDFClientHandler handler = new BDFClientHandler(socket);
				reader.addListener(handler);
				
				synchronized(connectedClients) {
					connectedClients.incrementAndGet();
				}
				
				String name = String.format("BDFClient_%s", connectedClients);
				
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
		
		if(reader.isRunning()) {
			reader.stop();
		}
	}

	public int getConnectedClients() {
		return connectedClients.get();
	}
	
	/**
	 * BDFClientHandler handles unique client connections. The handler receives BDFSamples from the BDFReader and 
	 * convert these into bytes before sending them over the network, mimicking Biosemi's ActiView network connection.
	 */
	public class BDFClientHandler implements Runnable, BDFListener {
		
		// the input stream to keep the connection open
		private InputStream in;
		
		// the output stream to send data to
		private OutputStream out;
		
		// if the handler is connected
		private boolean connected = true; 
		
		public BDFClientHandler(Socket socket) throws IOException {
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
			
			reader.removeListener(this);
			System.out.println(String.format("%s disconnected.", Thread.currentThread().getName()));
		}
		
		@Override
		public void receivedSample(BDFSample sample) {
			byte[] data = new byte[sample.values.length * 3];
			
			for(int i=0; i<sample.values.length; i++) {
				int value = sample.values[i];
				
				data[i*3 + 0] = (byte) (value & 0xFF);
				data[i*3 + 1] = (byte) ((value >> 8) & 0xFF);
				data[i*3 + 2] = (byte) ((value >>> 16) & 0xFF);
			}
			
			try {
				out.write(data);
			} catch (IOException e) {
				connected = false;
			}
		}
	}
	
	public static void printUsage() {
		System.out.println("Usage: BDFServer [-r] FILE PORT [FREQUENCY]");
		System.out.println("-r        : repeat the file when the end is reached.");
		System.out.println("FILE      : the BDF file.");
		System.out.println("PORT      : port number for connecting clients.");
		System.out.println("FREQUENCY : the frequency for sending data. 0 for full speed, -1 for file based sample rate (default).");		
	}
	
	public static void main(String[] args) throws IOException, BDFException {
		if(args.length < 2) {
			printUsage();
			return;
		}
		
		String pathname	= "";
		boolean repeat	= false;
		int frequency	= -1;
		
		int PORT		= 0;

		if(args[0].equals("-r")) {
			if(args.length < 3) {
				printUsage();
				return;
			}
			
			pathname 	= args[1];
			repeat 	= true;
			
			if(args.length > 3) {
				frequency = Integer.parseInt(args[3]);
			}
			
			PORT = Integer.parseInt(args[2]);
		} else {
			pathname = args[0];
			
			if(args.length > 2) {
				frequency = Integer.parseInt(args[2]);
			}
			
			PORT = Integer.parseInt(args[1]);
		}
		
		BDFFile bdf = BDFFile.open(pathname);
			
		BDFReader reader = bdf.getReader();
		reader.setRepeat(repeat);
		reader.setFrequency(frequency);
			
		BDFServer server = new BDFServer(reader, PORT);
		server.start();
	}
}