package it.hakvoort.neuroclient.reply;

import it.hakvoort.neuroclient.agent.Agent.Role;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 * 
 */
public class RoleReply extends DefaultReply {

	public Role role;
	
	public RoleReply() {
		
	}
	
	public RoleReply(ResponseCode response, Role role) {
		super(response);
		
		this.role = role;
	}
	
	@Override
	public String toString() {		
		return String.format("response: %s, role: %s", response, role);
	}
	
}
