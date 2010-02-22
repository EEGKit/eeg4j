package it.hakvoort.bdf.example;

import it.hakvoort.bdf.BDFDataRecord;
import it.hakvoort.bdf.BDFListener;
import it.hakvoort.bdf.BDFReader;


/**
 * 
 * @author Gido Hakvoort (gido@hakvoort.it)
 *
 */
public class JavaBDFReader implements BDFListener {

	private BDFReader bdfReader;
	
	public JavaBDFReader() {
		bdfReader = new BDFReader("data/bdf/example.bdf");
		
		bdfReader.setFrequency(1);
		bdfReader.setRepeat(true);
		
		bdfReader.addListener(this);
		
		bdfReader.start();
	}
	
	public static void main(String[] args) {		
		JavaBDFReader reader = new JavaBDFReader();
	}

	@Override
	public void receivedRecord(BDFDataRecord record) {
		System.out.println(record.toString());
	}
}
