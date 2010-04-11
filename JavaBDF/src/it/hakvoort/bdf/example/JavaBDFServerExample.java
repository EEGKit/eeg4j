package it.hakvoort.bdf.example;

import it.hakvoort.bdf.BDFReader;
import it.hakvoort.bdf.network.BDFServer;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class JavaBDFServerExample {

	public JavaBDFServerExample() {
		
		// create a BDFReader
		BDFReader reader = new BDFReader("data/bdf/example.bdf");
		reader.setRepeat(true);
		
		// create and start a BDFNetworkServer
		BDFServer server = new BDFServer(reader, 4321);
		
		// start the server
		server.start();
	}
	
	public static void main(String[] args) {
		new JavaBDFServerExample();
	}
}
