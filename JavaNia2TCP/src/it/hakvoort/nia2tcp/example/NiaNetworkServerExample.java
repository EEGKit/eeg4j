package it.hakvoort.nia2tcp.example;

import it.hakvoort.nia.NiaDevice;
import it.hakvoort.nia2tcp.NiaNetworkServer;

public class NiaNetworkServerExample {
	
	public NiaNetworkServerExample() {
		NiaDevice device = new NiaDevice();
		
		NiaNetworkServer server = new NiaNetworkServer(device, "localhost", 4321);
		
		server.start();
	}
	
	public static void main(String[] args) {
		new NiaNetworkServerExample();
	}
}
