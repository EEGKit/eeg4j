package it.hakvoort.neuroclient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 * 
 */
public class NeuroServerConnection {

	private static final String CRLF = "\r\n";

	// NeuroServer commands representation
	public static enum Command {
		HELLO, CLOSE, STATUS, ROLE, EEG, DISPLAY, CONTROL, WATCH, UNWATCH, SET_HEADER, GET_HEADER, SEND_PACKET, NONE
	};

	// NeuroServer commands
	public static Map<Command, String> commands = new HashMap<Command, String>();

	// A socket connection to NeuroServer
	private Socket socket;

	// The input and output streams for this connection
	private BufferedReader input;
	private DataOutputStream output;

	// The default host and port to use for this connection
	private String HOST = "localhost";
	private int PORT = 8336;

	private boolean connected = false;

	// listeners waiting for line input
	protected List<NeuroServerInputListener> inputListeners = new CopyOnWriteArrayList<NeuroServerInputListener>();
	
	// listeners waiting for status updates
	protected List<NeuroServerStatusListener> statusListeners = new CopyOnWriteArrayList<NeuroServerStatusListener>();
	
	private NeuroServerLineReader lineReader = new NeuroServerLineReader();

	static {
		commands.put(Command.HELLO, "hello");
		commands.put(Command.CLOSE, "close");
		commands.put(Command.STATUS, "status");
		commands.put(Command.ROLE, "role");
		commands.put(Command.EEG, "eeg");
		commands.put(Command.DISPLAY, "display");
		commands.put(Command.CONTROL, "control");
		commands.put(Command.WATCH, "watch");
		commands.put(Command.UNWATCH, "unwatch");
		commands.put(Command.SET_HEADER, "setheader");
		commands.put(Command.GET_HEADER, "getheader");
		commands.put(Command.SEND_PACKET, "!");
	}

	public NeuroServerConnection() {

	}

	public NeuroServerConnection(String HOST) {
		this.HOST = HOST;
	}

	public NeuroServerConnection(String HOST, int PORT) {
		this.HOST = HOST;
		this.PORT = PORT;
	}

	public void connect() {
		try {
			socket = new Socket();

			socket.connect(new InetSocketAddress(HOST, PORT), 5000);

			input = new BufferedReader(new InputStreamReader(socket .getInputStream()));
			output = new DataOutputStream(socket.getOutputStream());

			connected = true;
		} catch (UnknownHostException e) {
			System.err.println(String.format("Unknown Host: %s", HOST));
		} catch (IOException e) {
			System.err.println(String.format("Could not connect to %s:%s", HOST, PORT));
		}

		// if connected, start the input line reader
		if (connected) {
			lineReader.start();
		}
	}

	public void disconnect() {
		if (!connected) {
			return;
		}

		connected = false;
		fireDisconnected();

		try {
			input.close();
			output.close();
			socket.close();
		} catch (IOException e) {
			System.err.println(String.format("Error during disconnecting"));
		}
		
	}

	public boolean isConnected() {
		return this.connected;
	}

	public void sendCommand(Command command) {
		sendCommand(command, null);
	}

	public void sendCommand(Command command, int value) {
		sendCommand(command, Integer.toString(value));
	}

	public void sendCommand(Command command, String value) {
		if (!connected) {
			System.err.println(String.format("Not connected with NeuroServer, could not send command '%s'.", command));
			return;
		}

		if (!commands.containsKey(command)) {
			System.err.println(String.format("Invalid command '%s'.", command));
			return;
		}

		String data = commands.get(command);

		if (value != null) {
			data += " " + value;
		}

		data += NeuroServerConnection.CRLF;

		try {
			output.writeBytes(data);
		} catch (IOException e) {
			System.err.println(String.format("Could not send command: %s", command));
		}
	}

	public void addInputListener(NeuroServerInputListener listener) {
		inputListeners.add(listener);
	}

	public void removeInputListener(NeuroServerInputListener listener) {
		inputListeners.remove(listener);
	}
	
	public void addStatusListener(NeuroServerStatusListener listener) {
		statusListeners.add(listener);
	}

	public void removeStatusListener(NeuroServerStatusListener listener) {
		statusListeners.remove(listener);
	}

	public void removeAllInputListeners() {
		inputListeners.clear();
	}

	public void removeAllStatusListeners() {
		statusListeners.clear();
	}
	
	public List<NeuroServerInputListener> getInputListeners() {
		return inputListeners;
	}

	public List<NeuroServerStatusListener> getStatusListeners() {
		return statusListeners;
	}
	
	/**
	 * Send the received line to all listeners
	 */
	public void fireReceivedLine(String line) {
		if (line == null) {
			return;
		}

		for (NeuroServerInputListener listener : inputListeners) {
			listener.receivedLine(line);
		}
	}
	
	/**
	 * Send the received line a disconnected notification
	 */
	public void fireDisconnected() {
		for (NeuroServerStatusListener listener : statusListeners) {
			listener.disconnected();
		}
	}

	private class NeuroServerLineReader extends Thread {

		public void run() {
			while (connected) {
				try {
					fireReceivedLine(input.readLine());
				} catch (IOException e) {
					if (connected) {
						System.err.println(String.format("Error while reading line"));
						connected = false;
						fireDisconnected();
					}
				}
			}

			System.err.println(String.format("NeuroServerLineReader: No longer connected, stopped reading data."));
		}
	}
}