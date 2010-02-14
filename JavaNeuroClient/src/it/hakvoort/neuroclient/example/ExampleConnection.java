package it.hakvoort.neuroclient.example;

import it.hakvoort.neuroclient.NeuroServerConnection;
import it.hakvoort.neuroclient.NeuroServerInputListener;
import it.hakvoort.neuroclient.NeuroServerStatusListener;
import it.hakvoort.neuroclient.NeuroServerConnection.Command;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 * 
 */
public class ExampleConnection implements NeuroServerInputListener, NeuroServerStatusListener {
	
	public ExampleConnection() {
	
	}
	
	public static void main(String[] args) {
		// create a new NeuroServerConnection
		NeuroServerConnection connection = new NeuroServerConnection("localhost", 8336);
		
		// register this class as listener
		connection.addInputListener(new ExampleConnection());
		
		// connect to NeuroServer
		connection.connect();
		
		// check if connection is connected
		if(connection.isConnected()) {
			// send hello command
			connection.sendCommand(Command.HELLO);
		}
		
		// disconnect from NeuroServer
		connection.disconnect();
	}
	
	@Override
	public void receivedLine(String line) {
		// output incoming lines from NeuroServer
		System.out.println(line);
	}
	
	@Override
	public void disconnected() {
		
	}
}
