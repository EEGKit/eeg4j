package it.hakvoort.bdf.example;

import java.io.IOException;

import it.hakvoort.bdf.BDFException;
import it.hakvoort.bdf.BDFFile;
import it.hakvoort.bdf.BDFSample;
import it.hakvoort.bdf.BDFListener;
import it.hakvoort.bdf.BDFReader;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class JavaBDFReaderExample implements BDFListener {

	public JavaBDFReaderExample() throws IOException, BDFException {
		// create a BDFFile
		BDFFile bdf = BDFFile.open("data/bdf/example.bdf");
		
		// get the bdf reader
		BDFReader reader = bdf.getReader();
		
		// register this class as listener on the reader
		reader.addListener(this);
		
		// start the reader
		reader.start();
	}
	
	public static void main(String[] args) throws IOException, BDFException {
		new JavaBDFReaderExample();
	}

	@Override
	public void receivedSample(BDFSample sample) {
		System.out.println(sample.toString());
	}
}
