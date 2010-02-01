package it.hakvoort.neuroclient.agent;

import it.hakvoort.neuroclient.NeuroServerConnection;
import it.hakvoort.neuroclient.NeuroServerInputListener;
import it.hakvoort.neuroclient.NeuroServerConnection.Command;
import it.hakvoort.neuroclient.reply.DefaultReply;
import it.hakvoort.neuroclient.reply.Reply;
import it.hakvoort.neuroclient.reply.RoleReply;
import it.hakvoort.neuroclient.reply.StatusReply;
import it.hakvoort.neuroclient.reply.Reply.ResponseCode;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 * 
 */
public abstract class DefaultAgent implements Agent, NeuroServerInputListener {

	protected NeuroServerConnection connection = null;

	protected boolean finished = false;
	protected Command current = null;
	protected Reply reply = null;

	public DefaultAgent() {
		connection = new NeuroServerConnection();
		connection.connect();

		if (connection.isConnected()) {
			connection.addListener(this);
			init();
		}
	}

	public DefaultAgent(String HOST) {
		connection = new NeuroServerConnection(HOST);
		connection.connect();

		if (connection.isConnected()) {
			connection.addListener(this);
			init();
		}
	}

	public DefaultAgent(String HOST, int PORT) {
		connection = new NeuroServerConnection(HOST, PORT);
		connection.connect();

		if (connection.isConnected()) {
			connection.addListener(this);
			init();
		}
	}

	protected abstract void init();

	@Override
	public Reply hello() {
		return executeCommand(Command.HELLO);
	}

	@Override
	public Reply close() {
		Reply reply = executeCommand(Command.CLOSE);

		connection.disconnect();
		connection.removeListener(this);

		return reply;
	}

	@Override
	public StatusReply getStatus() {
		return (StatusReply) executeCommand(Command.STATUS);
	}

	@Override
	public RoleReply getRole() {
		return (RoleReply) executeCommand(Command.ROLE);
	}

	protected void setRole(Role role) {
		switch (role) {
		case EEG:
			executeCommand(Command.EEG);
			break;
		case DISPLAY:
			executeCommand(Command.DISPLAY);
			break;
		case CONTROL:
			executeCommand(Command.CONTROL);
			break;
		}

		if (reply.getResponseCode() != ResponseCode.OK) {
			System.err.println(String.format("Could not set role: %s", role));
		}
	}

	public Reply executeCommand(Command command) {
		return executeCommand(command, null);
	}

	public Reply executeCommand(Command command, int value) {
		return executeCommand(command, Integer.toString(value));
	}

	public Reply executeCommand(Command command, String value) {
		if (!connection.isConnected()) {
			return null;
		}

		reply = null;
		finished = false;
		current = command;

		connection.sendCommand(command, value);

		if (!finished) {
			synchronized (this) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		finished = false;
		current = Command.NONE;
		return reply;
	}

	@Override
	public void receivedLine(String line) {
		switch (current) {
		case ROLE:
			parseRole(line.toLowerCase());
			break;
		case STATUS:
			parseStatus(line.toLowerCase());
			break;
		default:
			parseDefault(line.toLowerCase());
		}

		if (finished) {
			synchronized (this) {
				this.notifyAll();
			}
		}
	}

	public void parseDefault(String line) {
		if (line.startsWith("200")) {
			reply = new DefaultReply(ResponseCode.OK);
		} else if (line.startsWith("400")) {
			reply = new DefaultReply(ResponseCode.ERROR);
		} else {
			System.err.println(String.format("Expected '200 OK' or '400 ERROR', but got: %s", line));
			reply = null;
		}

		finished = true;
	}

	private void parseRole(String line) {
		if (reply == null) {
			reply = new RoleReply();

			if (line.startsWith("200")) {
				reply.setResponseCode(ResponseCode.OK);
			} else if (line.startsWith("400")) {
				reply.setResponseCode(ResponseCode.ERROR);
				finished = true;
			} else {
				System.err.println(String.format("Expected '200 OK' or '400 ERROR', but got: %s", line));
				finished = true;
				reply = null;
			}
		} else {
			if (line.equals("eeg")) {
				((RoleReply) reply).role = Role.EEG;
			} else if (line.equals("display")) {
				((RoleReply) reply).role = Role.DISPLAY;
			} else if (line.equals("controller")) {
				((RoleReply) reply).role = Role.CONTROL;
			} else if (line.equals("unknown")) {
				((RoleReply) reply).role = Role.UNKNOWN;
			} else {
				System.err.println(String.format("Expected 'eeg', 'display', 'controller' or 'unknown', but got: %s", line));
				reply = null;
			}

			finished = true;
		}
	}

	private void parseStatus(String line) {
		if (reply == null) {
			reply = new StatusReply();

			if (line.startsWith("200")) {
				reply.setResponseCode(ResponseCode.OK);
			} else if (line.startsWith("400")) {
				reply.setResponseCode(ResponseCode.ERROR);
				finished = true;
			} else {
				System.err.println(String.format("Expected '200 OK' or '400 ERROR', but got: %s", line));
				finished = true;
				reply = null;
			}
		} else if (((StatusReply) reply).connectedClients == 0) {
			if (line.endsWith("clients connected")) {
				int connectedClients = Integer.valueOf(line.substring(0, line.indexOf(" ")));
				((StatusReply) reply).connectedClients = connectedClients;
				((StatusReply) reply).clients = new Role[connectedClients];
			} else {
				System.err.println(String.format("Expected '# clients connected', but got: %s", line));
				finished = true;
				reply = null;
			}
		} else {
			String result[] = line.split(":");

			int idx = Integer.valueOf(result[0]);

			if (idx > ((StatusReply) reply).connectedClients - 1) {
				System.err.println(String.format("Number of connected clients do not match."));
				finished = true;
				reply = null;
				return;
			}

			Role role = Role.UNKNOWN;

			if (result[1].equals("eeg")) {
				role = Role.EEG;
			} else if (result[1].equals("display")) {
				role = Role.DISPLAY;
			} else if (result[1].equals("controller")) {
				role = Role.CONTROL;
			} else if (result[1].equals("unknown")) {
				role = Role.UNKNOWN;
			} else {
				System.err.println(String.format("Expected 'eeg', 'display', 'controller' or 'unknown', but got: %s", result[1]));
			}

			((StatusReply) reply).clients[idx] = role;

			if (idx == ((StatusReply) reply).connectedClients - 1) {
				finished = true;
			}
		}
	}
}
