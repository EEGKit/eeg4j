package it.hakvoort.nia2tcp.example;

import it.hakvoort.nia.NiaListener;
import it.hakvoort.nia.NiaSample;
import it.hakvoort.nia2tcp.NiaNetworkClient;

public class NiaNetworkClientExample implements NiaListener {

	public NiaNetworkClientExample() {
		NiaNetworkClient client = new NiaNetworkClient("localhost", 4321);
		client.addListener(this);
		
		client.connect();
	}
	
	@Override
	public void receivedSample(NiaSample sample) {
		System.out.println(sample.toString());
	}
	
	public static void main(String[] args) {
		new NiaNetworkClientExample();
	}
}
