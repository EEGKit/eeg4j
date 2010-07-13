package it.hakvoort.bdf.example;

import java.io.IOException;

import it.hakvoort.bdf.BDFException;
import it.hakvoort.bdf.BDFFile;
import it.hakvoort.bdf.BDFReader;
import it.hakvoort.bdf.network.BDFServer;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class JavaBDFServerExample {

	public JavaBDFServerExample() throws IOException, BDFException {
		
		// create a BDFFile
		BDFFile bdf = BDFFile.open("data/bdf/example.bdf");
		
		// get the bdf reader
		BDFReader reader = bdf.getReader();
		
		// set the reader to start at the beginning of the file when it reached the end
		reader.setRepeat(true);
		
		// create and start a BDFNetworkServer
		BDFServer server = new BDFServer(reader, 4321);
		
		// start the server
		server.start();
	}
	
	public static void main(String[] args) {
		try {
			new JavaBDFServerExample();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BDFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
