package it.hakvoort.bdf.example;

import it.hakvoort.bdf.BDFDataRecord;
import it.hakvoort.bdf.BDFListener;
import it.hakvoort.bdf.network.BDFClient;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class JavaBDFClientExample implements BDFListener {
	
	public JavaBDFClientExample() {

		// create a BDFNetworkClient
		BDFClient client = new BDFClient("localhost", 4322, 33);
		
		// register this class as listener on the client
		client.addListener(this);

		// connect the client to the BDF server
		client.connect();
	}

	@Override
	public void receivedRecord(BDFDataRecord record) {
		System.out.println(record.toString());
	}
	
	public static void main(String[] args) {
		new JavaBDFClientExample();
	}
}
