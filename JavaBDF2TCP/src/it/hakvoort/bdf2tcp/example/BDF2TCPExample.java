package it.hakvoort.bdf2tcp.example;

import it.hakvoort.bdf.BDFDataRecord;
import it.hakvoort.bdf.BDFListener;
import it.hakvoort.bdf.BDFReader;
import it.hakvoort.bdf2tcp.BDFNetworkClient;
import it.hakvoort.bdf2tcp.BDFNetworkServer;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class BDF2TCPExample implements BDFListener {
	
	public BDF2TCPExample() {	
		// create a BDFReader
		BDFReader reader = new BDFReader("data/bdf/example.bdf");
		
		// create and start a BDFNetworkServer
		BDFNetworkServer server = new BDFNetworkServer(reader, "localhost", 4321);
		server.start();
		
		// wait a second
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// The number of channels which are send by the server and read by the client must be the same! If not, data will be incorrect. 
		int channels = reader.getHeader().getNumChannels();

		// create a BDFNetworkClient
		BDFNetworkClient client = new BDFNetworkClient("localhost", 4321, channels);
		client.addListener(this);

		// connect the client
		client.connect();
	}
	
	@Override
	public void receivedRecord(BDFDataRecord record) {
		System.out.println(record.toString());
	}
	
	public static void main(String[] args) {		
		new BDF2TCPExample();
	}
}
