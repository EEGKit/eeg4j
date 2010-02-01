package it.hakvoort.neuroclient;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 * 
 */
public interface NeuroServerPacketListener {
	
	public void receivedPacket(NeuroServerPacket packet);
	
}
