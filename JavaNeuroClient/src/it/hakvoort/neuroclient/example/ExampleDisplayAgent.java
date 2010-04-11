package it.hakvoort.neuroclient.example;

import it.hakvoort.neuroclient.NeuroServerPacket;
import it.hakvoort.neuroclient.NeuroServerPacketListener;
import it.hakvoort.neuroclient.agent.DisplayAgent;
import it.hakvoort.neuroclient.reply.HeaderReply;
import it.hakvoort.neuroclient.reply.RoleReply;
import it.hakvoort.neuroclient.reply.StatusReply;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 * 
 */
public class ExampleDisplayAgent implements NeuroServerPacketListener {
	
	public ExampleDisplayAgent() {
	
	}
	
	public static void main(String[] args) {
		// create a new DisplayAgent
		DisplayAgent agent = new DisplayAgent("localhost", 8336);
		
		// register this class as listener
		agent.addPacketListener(new ExampleDisplayAgent());
		
		// get the status of NeuroServer
		StatusReply statusReply = agent.getStatus();
		
		// get the header of client 0
		HeaderReply headerReply = agent.getHeader(0);
		
		// get the role of this agent
		RoleReply roleReply = agent.getRole();
		
		// start watching client 0
		agent.watch(0);
		
		// stop watching client 0
		agent.unwatch(0);		

		// close the agent and disconnect from NeuroServer.
		agent.close();	
	}

	@Override
	public void receivedPacket(NeuroServerPacket packet) {
		// output the received packet
		System.out.println(packet.toString());
	}
}
