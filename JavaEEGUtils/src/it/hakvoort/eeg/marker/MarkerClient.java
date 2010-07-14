package it.hakvoort.eeg.marker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class MarkerClient implements Runnable {
	
	// the client socket
	private Socket socket;
	
	// the input stream to keep the connection open
	private InputStream in;
	
	// the output stream to send the marker to
	private OutputStream out;
	
	// the hostname or ip address of the server
	private String HOST;
	
	// the server port
	private int PORT;
	
	// if the client is connected
	private boolean connected = false;

	public MarkerClient(String HOST, int PORT) {
		this.HOST = HOST;
		this.PORT = PORT;
	}
	
	public void connect() throws IOException {
		socket = new Socket();
		socket.connect(new InetSocketAddress(HOST, PORT), 5000);
		
		in 	= socket.getInputStream();
		out = socket.getOutputStream();

		connected = true;
		
		new Thread(this).start();
	}
	
	public void disconnect() {
		if(!connected) {
			return;
		}

		connected = false;

		try {
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			System.err.println(String.format("Disconnect error"));
		}
	}
	
	public boolean isConnected() {
		return this.connected;
	}

	public void sendMarker(byte marker) {
		try {
			out.write(marker);
		} catch (IOException e) {
			connected = false;
		}
	}

	public void run() {
		while(connected) {
			try {
				while(connected && in.read() != -1) {}
			} catch (IOException e) {
				connected = false;
			}
		}
	}
}
