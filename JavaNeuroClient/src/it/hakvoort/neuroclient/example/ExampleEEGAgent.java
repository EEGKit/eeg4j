package it.hakvoort.neuroclient.example;

import it.hakvoort.neuroclient.EDFHeader;
import it.hakvoort.neuroclient.NeuroServerPacket;
import it.hakvoort.neuroclient.agent.EEGAgent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 * 
 */
public class ExampleEEGAgent {
	
	public ExampleEEGAgent() {
	
	}
	
	public static void main(String[] args) {
		// create a new EEGAgent
		EEGAgent agent = new EEGAgent("localhost", 8336);
		
		// create a properties file
		Properties properties = new Properties();
		
		try {
			// load the properties from a file.
			properties.load(new FileInputStream("etc/header.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// create a new EDFHeader with the properties
		EDFHeader header = new EDFHeader(properties);
		
		// set the agent's header
		agent.setHeader(header);
		
		// send a EEG Data Packet to NeuroServer
		agent.sendPacket(new NeuroServerPacket(1, 3, new int[]{125, 129, 111}));
				
		// close the agent and disconnect from NeuroServer.
		agent.close();	
	}
}
