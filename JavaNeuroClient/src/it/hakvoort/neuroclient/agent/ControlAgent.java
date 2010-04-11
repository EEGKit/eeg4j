package it.hakvoort.neuroclient.agent;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 * 
 */
public class ControlAgent extends DefaultAgent {

	public ControlAgent() {
		super();
	}
	
	public ControlAgent(String HOST) {
		super(HOST);
	}
	
	public ControlAgent(String HOST, int PORT) {
		super(HOST, PORT);
	}
	
	protected void init() {
		setRole(Role.CONTROL);
	}
}