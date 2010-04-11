package it.hakvoort.neuroclient.reply;

import it.hakvoort.neuroclient.agent.Agent.Role;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 * 
 */
public class StatusReply extends DefaultReply {
	
	public int connectedClients = 0;
	public Role clients[];
	
	public StatusReply() {
		
	}
	
	public StatusReply(ResponseCode response, int connectedClients, Role clients[]) {
		super(response);
		
		this.connectedClients = connectedClients;
		this.clients = clients;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(String.format("response: %s, clients[%s]: {", response, connectedClients));
		for(int i=0; i<connectedClients; i++) {
			buffer.append(String.format("%s:%s", i, clients[i]));
			
			if(i < connectedClients-1) {
				buffer.append(", ");
			}
		}
		buffer.append("}");
		
		return buffer.toString();
	}
}
