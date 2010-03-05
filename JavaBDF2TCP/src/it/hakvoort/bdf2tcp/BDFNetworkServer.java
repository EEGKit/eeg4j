package it.hakvoort.bdf2tcp;

import it.hakvoort.bdf.BDFDataRecord;
import it.hakvoort.bdf.BDFListener;
import it.hakvoort.bdf.BDFReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class BDFNetworkServer implements Runnable {
	
	// the server socket
	private ServerSocket serverSocket;
	
	// the BDFReader
	private BDFReader reader;
	
	// the hostname or ip address of the server
	private String HOST;
	
	// the server port
	private int PORT;
	
	// if the server is listening for new connections
	private boolean listening = true;
	
	// the number of connected clients
	private AtomicInteger connectedClients = new AtomicInteger(0);
	
	public BDFNetworkServer(BDFReader reader, String HOST, int PORT) {
		this.reader = reader;
		
		this.HOST = HOST;
		this.PORT = PORT;
	}
	
	@Override
	public void run() {
		// if the BDFReader is not running, start the reader
		if(!reader.isRunning()) {
			reader.start();
		}
		
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(HOST, PORT));
			
			System.out.println(String.format("BDFServer ready and listening for connections on: %s:%s.", HOST, PORT));
			while(listening) {
				Socket socket = serverSocket.accept();
								
				BDFClientHandler handler = new BDFClientHandler(socket);
				reader.addListener(handler);
				
				synchronized(connectedClients) {
					connectedClients.incrementAndGet();
				}
				
				Thread handlerThread = new Thread(handler);
				handlerThread.setName(String.format("BDFClient_%s", connectedClients));
				handlerThread.setDaemon(true);
				handlerThread.start();
			}
			
		} catch(IOException e) {
			e.printStackTrace();
			System.err.println(String.format("Could not bind socket to address %s:%s", HOST, PORT));
		}
	}
	
	public void close() {
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
	 * BDFClientHandler handles unique client connections. The handler receives BDFDataRecords from the BDFReader and 
	 * convert these into bytes before sending them over the network, mimicing Biosemi's ActiView network connection.
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
		}
		
		@Override
		public void receivedRecord(BDFDataRecord record) {
			byte[] data = new byte[record.channelCount * 3];
			
			for(int i=0; i<record.channelCount; i++) {
				int sample = record.samples[i];
				
				data[i*3 + 0] = (byte) (sample & 0xFF);
				data[i*3 + 1] = (byte) ((sample >> 8) & 0xFF);
				data[i*3 + 2] = (byte) ((sample >>> 16) & 0xFF);
			}
			
			try {
				out.write(data);
			} catch (IOException e) {
				connected = false;
			}
		}
	}
}
