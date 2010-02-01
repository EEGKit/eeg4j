package it.hakvoort.neuroclient.agent;

import it.hakvoort.neuroclient.EDFHeader;
import it.hakvoort.neuroclient.NeuroServerPacket;
import it.hakvoort.neuroclient.NeuroServerConnection.Command;
import it.hakvoort.neuroclient.reply.Reply;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 * 
 */
public class EEGAgent extends DefaultAgent {

	public EEGAgent() {
		super();
	}

	public EEGAgent(String HOST) {
		super(HOST);
	}
	
	public EEGAgent(String HOST, int PORT) {
		super(HOST, PORT);
	}
	
	protected void init() {
		setRole(Role.EEG);
	}
	
	public Reply setHeader(EDFHeader header) {
		return executeCommand(Command.SET_HEADER, header.toString());
	}
	
	public void sendPacket(NeuroServerPacket packet) {
		// expect: 200 OK, but we don't care cause we don't want this function to block.
		// send it direct to the connection
		current = Command.SEND_PACKET;
		connection.sendCommand(Command.SEND_PACKET, packet.toString());
	}
	
	@Override
	public void receivedLine(String line) {
		switch(current) {
			case SEND_PACKET:
				// do nothing
				break;
			default:
				super.receivedLine(line);
		}
	}
}
