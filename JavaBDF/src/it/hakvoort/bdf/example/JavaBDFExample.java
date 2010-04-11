package it.hakvoort.bdf.example;

import it.hakvoort.bdf.BDFDataRecord;
import it.hakvoort.bdf.BDFListener;
import it.hakvoort.bdf.BDFReader;

/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class JavaBDFExample implements BDFListener {

	public JavaBDFExample() {
		// create a BDFReader
		BDFReader reader = new BDFReader("data/bdf/example.bdf");
		
		// register this class as listener on the reader
		reader.addListener(this);
		
		// start the reader
		reader.start();
	}
	
	public static void main(String[] args) {		
		new JavaBDFExample();
	}

	@Override
	public void receivedRecord(BDFDataRecord record) {
		System.out.println(record.toString());
	}
}
