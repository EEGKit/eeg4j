package it.hakvoort.neuroclient.example;

import it.hakvoort.neuroclient.NeuroServerPacket;
import it.hakvoort.neuroclient.NeuroServerPacketListener;
import it.hakvoort.neuroclient.agent.ControlAgent;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 * 
 */
public class ExampleControlAgent implements NeuroServerPacketListener {
	
	public ExampleControlAgent() {
	
	}
	
	public static void main(String[] args) {
		// create a new ControlAgent
		ControlAgent agent = new ControlAgent("localhost", 8336);
		
		// close the agent and disconnect from NeuroServer.
		agent.close();
	}

	@Override
	public void receivedPacket(NeuroServerPacket packet) {
		// output the received packet
		System.out.println(packet.toString());
	}
}
