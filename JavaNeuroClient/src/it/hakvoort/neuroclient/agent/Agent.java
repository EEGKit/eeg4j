package it.hakvoort.neuroclient.agent;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 * 
 */
import it.hakvoort.neuroclient.reply.Reply;
import it.hakvoort.neuroclient.reply.RoleReply;
import it.hakvoort.neuroclient.reply.StatusReply;

public interface Agent {
	
	// NeuroServer roles
	public enum Role {UNKNOWN, EEG, DISPLAY, CONTROL};
	
	public Reply hello();
	public Reply close();
	public StatusReply getStatus();
	public RoleReply getRole();
}