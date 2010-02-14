package it.hakvoort.neuroclient.agent;

import it.hakvoort.neuroclient.EDFHeader;
import it.hakvoort.neuroclient.NeuroServerPacket;
import it.hakvoort.neuroclient.NeuroServerPacketListener;
import it.hakvoort.neuroclient.NeuroServerConnection.Command;
import it.hakvoort.neuroclient.reply.HeaderReply;
import it.hakvoort.neuroclient.reply.Reply;
import it.hakvoort.neuroclient.reply.Reply.ResponseCode;

import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 * 
 */
public class DisplayAgent extends DefaultAgent {

	// listeners waiting for packets
	protected List<NeuroServerPacketListener> packetListeners = new CopyOnWriteArrayList<NeuroServerPacketListener>();

	public DisplayAgent() {
		super();
	}

	public DisplayAgent(String HOST) {
		super(HOST);
	}

	public DisplayAgent(String HOST, int PORT) {
		super(HOST, PORT);
	}

	protected void init() {
		setRole(Role.DISPLAY);
	}

	public Reply watch(int clientIndex) {
		return executeCommand(Command.WATCH, clientIndex);
	}

	public Reply unwatch(int clientIndex) {
		return executeCommand(Command.UNWATCH, clientIndex);
	}

	public HeaderReply getHeader(int clientIndex) {
		return (HeaderReply) executeCommand(Command.GET_HEADER, clientIndex);
	}

	public void addPacketListener(NeuroServerPacketListener listener) {
		packetListeners.add(listener);
	}

	public void removePacketListener(NeuroServerPacketListener listener) {
		packetListeners.remove(listener);
	}

	public void removeAllPacketListeners() {
		packetListeners.clear();
	}

	public List<NeuroServerPacketListener> getPacketListeners() {
		return packetListeners;
	}

	/**
	 * Send the received packet to all listeners
	 */
	private void fireReceivedPacket(NeuroServerPacket packet) {
		for (NeuroServerPacketListener listener : packetListeners) {
			listener.receivedPacket(packet);
		}
	}

	public NeuroServerPacket parsePacketLine(String line) {
		StringTokenizer tokenizer = new StringTokenizer(line);

		// Check if line is a valid packet line
		if (!tokenizer.nextToken().equals("!")) {
			System.err.println(String.format("Not a valid packet line: %s", line));
			return null;
		}

		int channelNumber = Integer.valueOf(tokenizer.nextToken());
		int packetCounter = Integer.valueOf(tokenizer.nextToken());
		int channelCount = Integer.valueOf(tokenizer.nextToken());

		int samples[] = new int[channelCount];

		for (int i = 0; i < channelCount; i++) {
			samples[i] = Integer.valueOf(tokenizer.nextToken());
		}

		return new NeuroServerPacket(packetCounter, channelCount, samples);
	}

	@Override
	public void receivedLine(String line) {
		if (!line.startsWith("!")) {
			switch (current) {
			case GET_HEADER:
				parseHeader(line);
				break;
			default:
				super.receivedLine(line);
			}

			if (finished) {
				synchronized (this) {
					this.notifyAll();
				}
			}
		} else {
			fireReceivedPacket(parsePacketLine(line));
		}
	}

	private void parseHeader(String line) {
		if (reply == null) {
			reply = new HeaderReply();

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
			byte[] main = line.substring(0, 256).getBytes();
			byte[] channels = line.substring(256).getBytes();
			
			EDFHeader header = new EDFHeader();
			
			header.setMainHeader(main);
			header.setChannelHeader(channels);
			
			((HeaderReply) reply).header = header;
			finished = true;
		}
	}
}
